package de.jowisoftware.mining.importer.redmine

import java.net.{ URLEncoder, URL }
import scala.Option.option2Iterable
import scala.annotation.tailrec
import scala.xml.{ XML, Node }
import org.joda.time.format.DateTimeFormat
import de.jowisoftware.mining.importer.TicketDataFields.{ updateDate, ticketType, summary, status, startDate, spentTime, reporter, relationships, progress, owner, id, eta, dueDate, description, creationDate, component }
import de.jowisoftware.mining.importer.{ TicketRelationship, TicketDataFields, TicketData, Importer, ImportEvents }
import de.jowisoftware.util.XMLUtils.{ NodeSeq2EnrichedNodeSeq, Node2EnrichedNode }
import scala.xml.NodeSeq

object RedmineImporter {
  val dateParser = DateTimeFormat.forPattern("yyyy-MM-dd")
  val timeParser = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
}

class RedmineImporter extends Importer with CachedItems {
  def userOptions = new RedmineOptions()

  def importAll(config: Map[String, String], events: ImportEvents) {
    processAllTickets(config, events)
  }

  @tailrec private def processAllTickets(config: Map[String, String], events: ImportEvents, start: Int = 0) {
    val page = retrieveXML("issues.xml",
      Map("offset" -> start.toString,
        "limit" -> "25",
        "project_id" -> config("project")),
      config)

    page \ "issue" foreach { node =>
      val xml = receiveTicket(node, config)
      processTicket(xml, config, events)
    }

    val total = (page \ "@total_count" text).toInt
    val newOffset = ((page \ "@offset" text).toInt + (page \ "@limit" text).toInt)

    if (newOffset < total) {
      processAllTickets(config, events, newOffset)
    }
  }

  def receiveTicket(ticketXML: Node, config: Map[String, String]) = {
    val id = (ticketXML \ "id" text).toInt

    retrieveXML("issues/"+id+".xml", Map(
      "include" -> "children,attachments,relations,changesets,journals"),
      config)
  }

  def processTicket(ticketXML: Node, config: Map[String, String], events: ImportEvents) {
    val baseTicket = createBaseTicket(ticketXML, config)
    println(baseTicket)
  }

  def createBaseTicket(ticketXML: Node, config: Map[String, String]) = {
    val ticket = new TicketData()
    val nodeId = (ticketXML \ "id" text).toInt
    ticket(id) = nodeId

    def conditionalSet[T](desc: TicketDataFields.FieldDescription[T],
      selector: Node => NodeSeq, evaluator: NodeSeq => T) {
      val result = selector(ticketXML)
      if (result.length > 0 && result.text != "") {
        ticket(desc) = evaluator(result)
      }
    }

    conditionalSet(ticketType, _ \ "tracker" \ "@id", id => getTracker(id.intText, config))
    conditionalSet(status, _ \ "status" \ "@id", id => getStatus(id.intText, config))
    conditionalSet(reporter, _ \ "author" \ "@id", id => getUser(id.intText, config))
    conditionalSet(owner, _ \ "assigned_to" \ "@id", id => getUser(id.intText, config))
    conditionalSet(component, _ \ "category" \ "@id", id => getCategory(id.intText, config))
    conditionalSet(summary, _ \ "subject", _.text)
    conditionalSet(description, _ \ "description", _.text)
    conditionalSet(startDate, _ \ "start_date", ts => RedmineImporter.dateParser.parseDateTime(ts text).toDate)
    conditionalSet(dueDate, _ \ "due_at", ts => RedmineImporter.dateParser.parseDateTime(ts text).toDate)
    conditionalSet(progress, _ \ "done_ratio", _.intText)
    conditionalSet(eta, _ \ "estimated_hours", _.intText)
    conditionalSet(spentTime, _ \ "spent_hours", _.floatText)
    conditionalSet(creationDate, _ \ "created_on", ts => RedmineImporter.timeParser.parseDateTime(ts.text).toDate)
    conditionalSet(updateDate, _ \ "update_on", ts => RedmineImporter.timeParser.parseDateTime(ts.text).toDate)

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

  def retrieveXML(file: String, request: Map[String, String], config: Map[String, String]) = {
    def encode(s: String) = URLEncoder.encode(s, "UTF-8")
    val params = request map { case (key, value) => encode(key)+"="+encode(value) } mkString "&"

    val rpcUrl = new URL(config("url") +
      (if (!config("url").endsWith("/")) "/" else "") +
      file +
      (if (params.isEmpty) "" else "?"+params))

    val connection = rpcUrl.openConnection()
    connection.setRequestProperty("Content-Type", "application/xml")
    connection.setRequestProperty("X-Redmine-API-Key", config("key"))
    XML.load(connection.getInputStream())
  }
}