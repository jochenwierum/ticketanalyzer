package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.mining.model.relationships._
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

    val versionNodes = ticketVersions.zipWithIndex.map {
      case (ticketData, version) =>
        debug("Adding ticket version "+version+"...")
        val ticket = createTicket(ticketData, version, commentList, repository)
        trace("Ticket node: "+ticket.id)
        ticket
    }

    debug("Connecting versions of ticket "+ticketVersions.head(id))
    connectVersions(versionNodes, versionNodes.last)
    debug("Connecting references from ticket "+ticketVersions.head(id))
    connectReferences(ticketVersions, versionNodes, repository)
    debug("Catching up missing links to this ticket...")
    connectMissingLinks(versionNodes.last, repository)
    debug("Ticket "+ticketVersions.head(id)+" finished")

    safePointReached
  }

  private def obtainComment(ticket: Ticket, comment: TicketCommentData) = {
    ticket.findComment(comment(TicketCommentDataFields.id)) match {
      case Some(comment) => comment
      case None =>
        debug("Adding comment "+comment(TicketCommentDataFields.id)+"...")
        createComment(ticket, comment)
    }
  }

  private def createComment(ticket: Ticket, comment: TicketCommentData): TicketComment = {
    val node = ticket.createComment(comment(TicketCommentDataFields.id))
    node.commentId(comment(TicketCommentDataFields.id))
    node.text(comment(TicketCommentDataFields.text))
    node.created(comment(TicketCommentDataFields.created))
    node.modified(comment(TicketCommentDataFields.modified))

    node.add(getPerson(comment(TicketCommentDataFields.author)), Created)

    node
  }

  private def createTicket(ticketData: TicketData, ticketVersion: Int,
    commentList: Seq[TicketCommentData], repository: TicketRepository) = {
    val ticket = repository.obtainTicket(ticketData(id), ticketVersion)
    ticket.title(ticketData(summary))
    ticket.text(ticketData(description))
    ticket.creationDate(ticketData(creationDate))
    ticket.updateDate(ticketData(updateDate))
    ticket.votes(ticketData(votes))
    ticket.eta(ticketData(eta))
    ticket.environment(ticketData(environment))
    ticket.build(ticketData(build))
    ticket.startDate(ticketData(startDate))
    ticket.dueDate(ticketData(dueDate))
    ticket.progress(ticketData(progress))
    ticket.spentTime(ticketData(spentTime))

    getPerson(ticketData(reporter)).add(ticket, Reported)
    ticket.add(getMilestone(ticketData(milestone)), InMilestone)
    ticket.add(getVersion(ticketData(version)), InVersion)
    ticket.add(getVersion(ticketData(fixedInVersion)), FixedInVersion)
    ticket.add(getVersion(ticketData(targetVersion)), Targets)
    ticket.add(getType(ticketData(ticketType)), HasType)
    ticket.add(getComponent(ticketData(component)), InComponent)
    ticket.add(getStatus(ticketData(status)), HasStatus)
    getPerson(ticketData(owner)).add(ticket, Owns)
    ticket.add(getResolution(ticketData(resolution)), HasResolution)
    ticket.add(getPriority(ticketData(priority)), HasPriority)
    ticket.add(getSeverity(ticketData(severity)), HasSeverity)
    ticket.add(getReproducability(ticketData(reproducability)), HasReproducability)

    ticketData(tags).foreach(tag => ticket.add(getTag(tag), HasTag))
    ticketData(sponsors).foreach(sponsor => getPerson(sponsor).add(ticket, Sponsors))

    ticketData(editor) match {
      case Some(user) =>
        val relationship = getPerson(user).add(ticket, ChangedTicket)
        relationship.changes(ticketData.updatedFields.filter(_ != "editor").toArray)
      case None =>
    }

    ticketData(comments).foreach { commentId =>
      commentList.indexWhere(_(TicketCommentDataFields.id) == commentId) match {
        case -1 =>
        case position =>
          val commentNode = obtainComment(ticket, commentList(position))
          ticket.add(commentNode, HasComment)
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
                val ref = transaction.getNode(id, Ticket).add(recentTicket, References)
                ref.referenceType(referenceType)
            }
            map.remove(recentTicket.ticketId())
        }
    }
  }

  private def connectVersions(ticketVersions: List[Ticket], rootVersion: Ticket) {
    def connect(next: Seq[Ticket]): Unit = next match {
      case recent :: head :: tail =>
        trace("Connecting node "+recent.id+" with Node "+head.id)
        head.add(recent, Updates)
        trace("Connecting node "+recent.id+" with Root "+rootVersion.id)
        root.add(recent, RootOf)
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
              val ref = headTicket.add(ticket, References)
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
