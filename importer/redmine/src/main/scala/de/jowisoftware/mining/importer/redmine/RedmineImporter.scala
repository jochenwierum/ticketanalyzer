package de.jowisoftware.mining.importer.redmine

import scala.Option.option2Iterable
import scala.xml.{ NodeSeq, Node, Elem }
import org.joda.time.format.DateTimeFormat
import de.jowisoftware.mining.importer._
import de.jowisoftware.mining.importer.TicketDataFields._
import de.jowisoftware.mining.importer.TicketRelationship
import de.jowisoftware.util.XMLUtils._
import grizzled.slf4j.Logging

object RedmineImporter {
  val dateParser = DateTimeFormat.forPattern("yyyy-MM-dd")
  val timeParser = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
}

private[redmine] class RedmineImporter(config: Map[String, String], events: ImportEvents)
    extends Logging {
  require(config contains "url")
  require(config contains "key")
  require(config contains "project")
  require(config contains "repositoryname")

  private val client = new RedmineClient(config("url"), config("key"))
  private val resolver = new CachedResolver
  private val contentCache = new FileCache

  def run() {
    info("Preparing import...")
    prepareImport
    info("Resolved data: "+resolver.dump)
    info("Importing tickets...")
    importTickets
    info("Importing finished.")
    events.finish
  }

  private def prepareImport {
    var count = 0L
    client.retrivePagedXML("issues.xml", Map("project_id" -> config("project"),
      "status_id" -> "*"),
      page => {
        page \ "issue" foreach { node =>
          val id = (node \ "id" text).toInt
          val ticketXML = client.retrieveXML("issues/"+id+".xml", Map(
            "include" -> "children,attachments,relations,changesets,journals"))
          contentCache.addChunk(ticketXML)
          fillCaches(ticketXML)
        }
        count += (page \ "issue").length
      })
    events.countedTickets(count)
  }

  private def fillCaches(ticketXML: Node) {
    var project = (ticketXML \ "project" \ "@id" text)
    ticketXML \\ "author" foreach { n => resolver.cacheUser(n) }
    ticketXML \\ "assigned_to" foreach { n => resolver.cacheUser(n) }
    ticketXML \ "status" foreach { n => resolver.cacheStatus(n) }
    ticketXML \ "tracker" foreach { n => resolver.cacheTracker(project, n \ "@name" text, n \ "@id" intText) }
    ticketXML \ "category" foreach { n => resolver.cacheCategory(n) }
    ticketXML \ "fixed_version" foreach { n => resolver.cacheVersion(n) }
    ticketXML \ "project" foreach { n => resolver.cacheProject(n) }

    ticketXML \ "journals" \ "journal" foreach { journal =>
      (journal \ "@name" text) match {
        case "tracker_id" => resolver.cacheTracker(project,
          journal \ "@name" text, journal \ "@id" intText)
        case "status_id" => resolver.cacheStatus(journal)
        case "assigned_to_id" => resolver.cacheUser(journal)
        case "category_id" => resolver.cacheCategory(journal)
        case "fixed_version_id" => resolver.cacheVersion(journal)

        case "project_id" => project = journal \ "old_value" text
        case _ =>
      }
    }
  }

  private def importTickets =
    contentCache.readChunks foreach { ticketXML =>
      processTicket(ticketXML)
    }

  private def receiveTicket(ticketXML: Node) = {
    val id = (ticketXML \ "id" text).toInt

    client.retrieveXML("issues/"+id+".xml", Map(
      "include" -> "children,attachments,relations,changesets,journals"))
  }

  private def processTicket(ticketXML: Node) {
    val project = (ticketXML \ "project" \ "@id" text)
    debug("Import ticket "+(ticketXML \ "id" text)+" in project "+project)

    val ticket = createTicket(ticketXML, project)
    val comments = findComments(ticketXML)
    debug("Parsing history")
    val updates = createUpdates(ticketXML, comments, project)

    debug("Creating history")
    val baseTicket = createBaseTicket(ticket, updates)
    val ticketHistory = createTicketHistory(baseTicket, updates)

    events.loadedTicket(config("repositoryname"), ticketHistory, comments)
  }

  private def createBaseTicket(ticket: TicketData, updates: Seq[Seq[Change]]) = {
    val baseTicket = new TicketData(ticket)
    updates.reverse.foreach(_.reverse.foreach {
      _.downgrade(baseTicket)
    })
    baseTicket
  }

  private def createTicketHistory(ticket: TicketData, updates: Seq[Seq[Change]]) = {
    def createTickets(lastTicket: TicketData, updates: List[Seq[Change]]): List[TicketData] = updates match {
      case changes :: tail =>
        val newTicket = new TicketData(lastTicket)
        changes.foreach(_.update(newTicket))
        newTicket :: createTickets(newTicket, tail)
      case Nil => Nil
    }

    ticket :: createTickets(ticket, updates.toList)
  }

  private def createTicket(ticketXML: Node, project: String) = {
    val ticket = new TicketData()
    val nodeId = (ticketXML \ "id" intText)
    ticket(id) = nodeId

    def conditionalSet[T](desc: TicketDataFields.FieldDescription[T],
      selector: Node => NodeSeq, evaluator: NodeSeq => T) {
      val result = selector(ticketXML)
      if (result.length > 0 && result.text != "") {
        ticket(desc) = evaluator(result)
      }
    }

    conditionalSet(ticketType, _ \ "tracker" \ "@id", id => resolver.tracker(project)(id.intText))
    conditionalSet(status, _ \ "status" \ "@id", id => resolver.status(id.intText))
    conditionalSet(reporter, _ \ "author" \ "@id", id => resolver.user(id.intText))
    conditionalSet(owner, _ \ "assigned_to" \ "@id", id => resolver.user(id.intText))
    conditionalSet(component, _ \ "category" \ "@id", id => resolver.category(id.intText))
    conditionalSet(summary, _ \ "subject", _.text)
    conditionalSet(description, _ \ "description", _.text)
    conditionalSet(startDate, _ \ "start_date", ts => RedmineImporter.dateParser.parseDateTime(ts text).toDate)
    conditionalSet(dueDate, _ \ "due_at", ts => RedmineImporter.dateParser.parseDateTime(ts text).toDate)
    conditionalSet(progress, _ \ "done_ratio", _.intText)
    conditionalSet(eta, _ \ "estimated_hours", _.floatText)
    conditionalSet(spentTime, _ \ "spent_hours", _.floatText)
    conditionalSet(creationDate, _ \ "created_on", ts => RedmineImporter.timeParser.parseDateTime(ts.text).toDate)
    conditionalSet(updateDate, _ \ "updated_on", ts => RedmineImporter.timeParser.parseDateTime(ts.text).toDate)
    conditionalSet(fixedInVersion, _ \ "fixed_version" \ "@id", id => resolver.version(id.intText))

    ticket(relationships) = ((ticketXML \ "children" \ "issue") map {
      node => TicketRelationship(node \ "@id" intText, TicketRelationship.RelationshipType.parentOf)
    }) ++ ((ticketXML \ "relations" \ "relation") flatMap {
      node =>
        if (node \ "@issue_id" == nodeId)
          Some(TicketRelationship(node \ "@id" intText,
            RedmineParser.parseRelation(node \ "@relation_type" text)))
        else
          None
    })

    ticket
  }

  private def findComments(ticketXML: Node) = {
    ticketXML \ "journals" \ "journal" flatMap { node =>
      val text = (node \ "notes" text)

      if (text.nonEmpty) {
        val id = (node \ "@id" intText)
        val author = resolver.user(node \ "user" \ "@id" intText)
        val created = RedmineImporter.timeParser.parseDateTime(node \ "created_on" text).toDate

        val comment = new TicketCommentData()
        comment(TicketCommentDataFields.id) = id
        comment(TicketCommentDataFields.author) = author
        comment(TicketCommentDataFields.modified) = created
        comment(TicketCommentDataFields.created) = created
        comment(TicketCommentDataFields.text) = text
        Some(comment)
      } else {
        None
      }
    }
  }

  private def createUpdates(ticketXML: Node, comments: Seq[TicketCommentData], project: String) = {
    ticketXML \ "journals" \ "journal" flatMap { node =>
      val id = (node \ "@id" intText)
      val hasComment = comments.find(_(TicketCommentDataFields.id) == id).isDefined
      try {
        Some(new ChangeParser(resolver, project).createChangesList(hasComment, node))
      } catch {
        case e: Exception =>
          error("Could not parse node fragment, ignoring this update: "+node.formatted, e)
          None
      }
    }
  }
}