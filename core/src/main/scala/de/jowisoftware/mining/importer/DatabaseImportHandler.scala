package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model._
import java.util.Date
import de.jowisoftware.neo4j.DBWithTransaction
import scala.collection.mutable
import grizzled.slf4j.Logging

class DatabaseImportHandler(db: DBWithTransaction[RootNode]) extends ImportEvents with Logging {
  private val root = db.rootNode

  /** Missing Links in the format repository -> (ticketId -> (nodeId, type)) */
  private var missingLinks: Map[String, mutable.Map[Int, List[(Long, String)]]] = Map()

  def finish() {
    if (missingLinks.exists(p => !p._2.isEmpty)) {
      error("There are unresolved links which could not be imported:\n"+missingLinks.toString)
    }
  }

  def countedTickets(count: Long) {}
  def countedCommits(count: Long) {}

  def loadedCommit(repositoryName: String, commitData: CommitData) = {
    val repository = getCommitRepository(repositoryName)

    val commit = repository.createCommit()
    commit.commitId(commitData.id)
    commit.date(commitData.date)
    commit.message(commitData.message)

    commit.add(getPerson(commitData.author))(Owns)

    commitData.files.foreach {
      case (filename, value) =>
        val file = getFile(repository, filename)
        val relation = commit.add(file)(ChangedFile)
        relation.editType(value)
    }

    repository.add(commit)(Contains)
  }

  def loadedTicket(repositoryName: String, ticketVersions: List[TicketData], commentList: Seq[TicketCommentData]) = {
    import TicketData.TicketField._

    info("Inserting ticket "+ticketVersions.head(id)+", "+ticketVersions.size+" versions, "+commentList.size+" commments")
    val repository = getTicketRepository(repositoryName)

    val commentMap = commentList.map { comment =>
      debug("Adding comment "+comment.id+"...")
      createComment(comment)
    }.map(comment => (comment.commentId(), comment.id)).toMap

    val versionNodes = ticketVersions.zipWithIndex.map {
      case (t, i) =>
        debug("Adding ticket version "+i+"...")
        val ticket = createTicket(t, commentMap, repository)
        trace("Ticket node: "+ticket.id)
        ticket
    }

    debug("Connecting versions of ticket "+ticketVersions.head(id))
    connectVersions(versionNodes.toList)
    debug("Connecting references from ticket "+ticketVersions.head(id))
    connectReferences(ticketVersions, versionNodes, repository)
    debug("Catching up missing links to this ticket...")
    connectMissingLinks(versionNodes.last, repository)
    debug("ticket "+ticketVersions.head(id)+" finished")
  }

  private def createComment(comment: TicketCommentData): TicketComment = {
    val node = db.createNode(TicketComment)

    node.commentId(comment.id)
    node.text(comment.text)
    node.created(comment.created)
    node.modified(comment.modified)

    node.add(getPerson(comment.author))(Wrotes)

    node
  }

  private def createTicket(ticketData: TicketData, commentsMap: Map[Int, Long], repository: TicketRepository) = {
    import TicketData.TicketField
    import TicketData.TicketField._

    val ticket = repository.createTicket()
    ticket.ticketId(ticketData(id))
    ticket.title(ticketData(summary))
    ticket.text(ticketData(description))
    ticket.creationDate(ticketData(creationDate))
    ticket.updateDate(ticketData(updateDate))
    ticket.votes(ticketData(votes))
    ticket.eta(ticketData(eta))
    ticket.environment(ticketData(environment))
    ticket.build(ticketData(build))

    ticket.add(getPerson(ticketData(reporter)))(ReportedBy)
    ticket.add(getMilestone(ticketData(milestone)))(InMilestone)
    ticket.add(getVersion(ticketData(version)))(InVersion)
    ticket.add(getVersion(ticketData(fixedInVersion)))(FixedInVersion)
    ticket.add(getVersion(ticketData(targetVersion)))(Targets)
    ticket.add(getType(ticketData(ticketType)))(HasType)
    ticket.add(getComponent(ticketData(component)))(InComponent)
    ticket.add(getStatus(ticketData(status)))(HasStatus)
    ticket.add(getPerson(ticketData(owner)))(Owns)
    ticket.add(getResolution(ticketData(resolution)))(HasResolution)
    ticket.add(getPriority(ticketData(priority)))(HasPriority)
    ticket.add(getSeverity(ticketData(severity)))(HasSeverity)
    ticket.add(getReproducability(ticketData(reproducability)))(HasReproducability)

    ticketData(tags).foreach(tag => ticket.add(getTag(tag))(HasTag))
    ticketData(sponsors).foreach(sponsor => ticket.add(getPerson(sponsor))(SponsoredBy))

    ticketData(comments).foreach { commentId =>
      commentsMap.get(commentId) match {
        case Some(id) => ticket.add(db.getNode(id)(TicketComment))(HasComment)
        case None =>
      }
    }

    ticket
  }

  private def connectVersions(ticketVersions: List[Ticket]) {
    def connect(next: Seq[Ticket]): Unit = next match {
      case recent :: head :: tail =>
        trace("Connecting node "+recent.id+" with Node "+head.id)
        recent.add(head)(Updates)
        connect(head :: tail)
      case recent :: Nil =>
    }
    connect(ticketVersions)
  }

  private def connectReferences(tickets: List[TicketData], versionNodes: List[Ticket], repository: TicketRepository) {
    import TicketData.TicketField._

    def connect(ticketsWithId: List[(TicketData, Ticket)]): Unit = ticketsWithId match {
      case (headTicketData, headTicket) :: tail =>
        headTicketData(relationships).foreach { rel =>
          val targetId = rel.toTicket
          // TODO: which version of the target should we select?
          val targetNode = findTicket(repository, targetId)

          targetNode match {
            case Some(ticket) =>
              trace("Adding connection from ticket "+targetId+" to this one")
              val ref = headTicket.add(ticket)(References)
              ref.referenceType(rel.ticketRelationship.toString)
            case None =>
              trace("Ticket "+targetId+" is not known yet - queuing operation up")
              val repositoryLinks = accuireMissingLinksForRepository(repository.name())
              repositoryLinks(targetId) = (headTicket.id, rel.ticketRelationship.toString) :: repositoryLinks.getOrElse(targetId, Nil)
          }
        }
        connect(tail)
      case Nil =>
    }
    connect(tickets.zip(versionNodes))
  }

  private def connectMissingLinks(recentTicket: Ticket, repository: TicketRepository) {
    missingLinks.get(repository.name()) match {
      case None =>
      case Some(map) =>
        map.get(recentTicket.ticketId()) match {
          case None =>
          case Some(list) =>
            list.foreach {
              case (id, referenceType) =>
                trace("Adding reference from already visited node "+id)
                val ref = db.getNode(id)(Ticket).add(recentTicket)(References)
                ref.referenceType(referenceType)
            }
            map.remove(recentTicket.ticketId())
        }
    }
  }

  private def getTicketRepository(name: String) =
    root.ticketRepositoryCollection.findOrCreateChild(name)
  private def getCommitRepository(name: String) =
    root.commitRepositoryCollection.findOrCreateChild(name)

  private def getMilestone(name: String) = root.milestoneCollection.findOrCreateChild(name)
  private def getVersion(name: String) = root.versionCollection.findOrCreateChild(name)
  private def getType(name: String) = root.typeCollection.findOrCreateChild(name)
  private def getTag(name: String) = root.tagCollection.findOrCreateChild(name)
  private def getComponent(name: String) = root.componentCollection.findOrCreateChild(name)
  private def getStatus(name: String) = root.statusCollection.findOrCreateChild(name)
  private def getPerson(name: String) = root.personCollection.findOrCreateChild(name)
  private def getResolution(name: String) = root.resolutionCollection.findOrCreateChild(name)
  private def getPriority(name: String) = root.priorityCollection.findOrCreateChild(name)
  private def getSeverity(name: String) = root.severityCollection.findOrCreateChild(name)
  private def getReproducability(name: String) = root.reproducabilityCollection.findOrCreateChild(name)

  private def getFile(repository: CommitRepository, name: String): File =
    repository.findFile(name) match {
      case Some(file) => file
      case None =>
        val file = repository.createFile()
        file.name(name)
        repository.add(file)(Contains)
        file
    }

  private def findTicket(repository: TicketRepository, id: Int) =
    repository.findRecentVersionOf(id)

  private def accuireMissingLinksForRepository(name: String) = missingLinks.get(name) match {
    case Some(map) => map
    case None =>
      val newMap: mutable.Map[Int, List[(Long, String)]] = mutable.Map()
      missingLinks += name -> newMap
      newMap
  }
}