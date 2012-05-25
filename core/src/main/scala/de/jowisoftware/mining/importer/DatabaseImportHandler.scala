package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model._
import java.util.Date
import de.jowisoftware.neo4j.DBWithTransaction

class DatabaseImportHandler(db: DBWithTransaction[RootNode]) extends ImportEvents {
  private val root = db.rootNode
  
  def finish() {}
  def countedTickets(count: Long) {}
  def countedCommits(count: Long) {}

  def loadedTicket(tickets: List[TicketData], comments: Seq[TicketCommentData]) = {
    val commentMap = comments.map(loadComment).map {comment => (comment.commentId(), comment.id)}.toMap
    tickets.foreach(t => loadTicket(t, commentMap))
  }
  
  private def loadComment(comment: TicketCommentData): TicketComment = {
    val node = db.createNode(TicketComment)
    
    node
  }
  
  private def loadTicket(ticketData: TicketData, commentsMap: Map[Int, Long]) {
    import TicketData.TicketField
    import TicketData.TicketField._
    
    val repository = getTicketRepository(ticketData(TicketField.repository))

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
    
    ticketData(comments).foreach(commentId => ticket.add(db.getNode(commentsMap(commentId))(TicketComment))(HasComment))
  }

  def loadedCommit(commitData: CommitData) = {
    val repository = getCommitRepository(commitData.repository)

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

  def getTicketRepository(name: String) =
    root.ticketRepositoryCollection.findOrCreateChild(name)
  def getCommitRepository(name: String) =
    root.commitRepositoryCollection.findOrCreateChild(name)
    
  def getMilestone(name: String) = root.milestoneCollection.findOrCreateChild(name)
  def getVersion(name: String) = root.versionCollection.findOrCreateChild(name)
  def getType(name: String) = root.typeCollection.findOrCreateChild(name)
  def getTag(name: String) = root.tagCollection.findOrCreateChild(name)
  def getComponent(name: String) = root.componentCollection.findOrCreateChild(name)
  def getStatus(name: String) = root.statusCollection.findOrCreateChild(name)
  def getPerson(name: String) = root.personCollection.findOrCreateChild(name)
  def getResolution(name: String) = root.resolutionCollection.findOrCreateChild(name)
  def getPriority(name: String) = root.priorityCollection.findOrCreateChild(name)
  def getSeverity(name: String) = root.severityCollection.findOrCreateChild(name)
  def getReproducability(name: String) = root.reproducabilityCollection.findOrCreateChild(name)
  
  def getFile(repository: CommitRepository, name: String): File =
    repository.findFile(name) match {
      case Some(file) => file
      case None =>
        val file = repository.createFile()
        file.name(name)
        repository.add(file)(Contains)
        file
    }
}