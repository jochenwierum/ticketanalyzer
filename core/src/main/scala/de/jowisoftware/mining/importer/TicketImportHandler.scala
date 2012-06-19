package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model._
import TicketDataFields._
import grizzled.slf4j.Logging
import scala.collection.mutable

private[importer] trait TicketImportHandler extends ImportEvents with Logging { this: GeneralImportHelper =>
  /** Missing ticket links in the format: repository -> (ticketId -> (nodeId, type)) */
  private var missingTicketLinks: Map[String, mutable.Map[Int, List[(Long, String)]]] = Map()

  def countedTickets(count: Long) {}

  abstract override def finish() = {
    if (missingTicketLinks.exists(p => !p._2.isEmpty)) {
      error("There are unresolved ticket links which could not be imported:\n"+missingTicketLinks.toString)
    }

    super.finish()
  }

  def loadedTicket(repositoryName: String, ticketVersions: List[TicketData], commentList: Seq[TicketCommentData]) = {
    info("Inserting ticket "+ticketVersions.head(id)+", "+ticketVersions.size+" versions, "+commentList.size+" commments")
    val repository = getTicketRepository(repositoryName)

    val commentMap = commentList.map { comment =>
      debug("Adding comment "+comment(TicketCommentDataFields.id)+"...")
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
    debug("Ticket "+ticketVersions.head(id)+" finished")

    safePointReached
  }

  private def createComment(comment: TicketCommentData): TicketComment = {

    val node = transaction.createNode(TicketComment)

    node.commentId(comment(TicketCommentDataFields.id))
    node.text(comment(TicketCommentDataFields.text))
    node.created(comment(TicketCommentDataFields.created))
    node.modified(comment(TicketCommentDataFields.modified))

    node.add(getPerson(comment(TicketCommentDataFields.author)))(Wrote)

    node
  }

  private def createTicket(ticketData: TicketData, commentsMap: Map[Int, Long], repository: TicketRepository) = {
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
        case Some(id) => ticket.add(transaction.getNode(id)(TicketComment))(HasComment)
        case None =>
      }
    }

    ticket
  }

  private def connectMissingLinks(recentTicket: Ticket, repository: TicketRepository) {
    missingTicketLinks.get(repository.name()) match {
      case None =>
      case Some(map) =>
        map.get(recentTicket.ticketId()) match {
          case None =>
          case Some(list) =>
            list.foreach {
              case (id, referenceType) =>
                trace("Adding reference from already visited node "+id)
                val ref = transaction.getNode(id)(Ticket).add(recentTicket)(References)
                ref.referenceType(referenceType)
            }
            map.remove(recentTicket.ticketId())
        }
    }
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
              val repositoryLinks = acquiremissingTicketLinksForRepository(repository.name())
              repositoryLinks(targetId) = (headTicket.id, rel.ticketRelationship.toString) :: repositoryLinks.getOrElse(targetId, Nil)
          }
        }
        connect(tail)
      case Nil =>
    }
    connect(tickets.zip(versionNodes))
  }

  private def acquiremissingTicketLinksForRepository(name: String) = missingTicketLinks.get(name) match {
    case Some(map) => map
    case None =>
      val newMap: mutable.Map[Int, List[(Long, String)]] = mutable.Map()
      missingTicketLinks += name -> newMap
      newMap
  }

  private def getMilestone(name: String) = root.milestoneCollection.findOrCreateChild(name)
  private def getVersion(name: String) = root.versionCollection.findOrCreateChild(name)
  private def getType(name: String) = root.typeCollection.findOrCreateChild(name)
  private def getTag(name: String) = root.tagCollection.findOrCreateChild(name)
  private def getComponent(name: String) = root.componentCollection.findOrCreateChild(name)
  private def getStatus(name: String) = root.statusCollection.findOrCreateChild(name)
  private def getResolution(name: String) = root.resolutionCollection.findOrCreateChild(name)
  private def getPriority(name: String) = root.priorityCollection.findOrCreateChild(name)
  private def getSeverity(name: String) = root.severityCollection.findOrCreateChild(name)
  private def getReproducability(name: String) = root.reproducabilityCollection.findOrCreateChild(name)

  private def findTicket(repository: TicketRepository, id: Int) =
    repository.findRecentVersionOf(id)
}
