package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model._
import java.util.Date

class DatabaseImportHandler(root: RootNode) extends ImportEvents {
  def finish() { }
  def countedTickets(count: Long) { }
  def countedCommits(count: Long) { }
  
  def loadedTicket(ticketData: TicketData) = {
    val repository = getTicketRepository(ticketData.repository)
    
    val ticket = repository.createTicket()
    ticket.id(ticketData.id)
    ticket.title(ticketData.summary)
    ticket.text(ticketData.description)
    ticket.creationDate(ticketData.creationDate)
    ticket.updateDate(ticketData.updateDate)
    
    ticket.add(getPerson(ticketData.reporter))(ReportedBy)
    ticket.add(getMilestone(ticketData.milestone))(InMilestone)
    ticket.add(getVersion(ticketData.version))(InVersion)
    ticket.add(getType(ticketData.ticketType))(HasType)
    ticket.add(getComponent(ticketData.component))(InComponent)
    ticket.add(getStatus(ticketData.status))(HasStatus)
    ticket.add(getPerson(ticketData.owner))(Owns)
    
    addUpdates(ticket, ticketData.updates)
  }
  
  def addUpdates(ticket: Ticket, updates: Seq[TicketUpdate]) = {
    for (update <- updates) {
      val updateNode = ticket.createUpdate(update.id)
      updateNode.time(update.time)
      updateNode.field(update.field)
      updateNode.value(update.newvalue)
      updateNode.oldvalue(update.oldvalue)
      
      updateNode.add(getPerson(update.author))(ChangedTicket)
    }
  }
  
  def loadedCommit(commitData: CommitData) = {
    val repository = getCommitRepository(commitData.repository)

    val commit = repository.createCommit()
    commit.id(commitData.id)
    commit.date(commitData.date)
    commit.message(commitData.message)
    
    commit.add(getPerson(commitData.author))(Owns)
    
    commitData.files.foreach{case (filename, value) =>
      val file = getFile(repository, filename)
      val relation = commit.add(file)(ChangedFile)
      relation.editType(value)
    }

    repository.add(commit)(Contains)
  }
  
  def getTicketRepository(name: String) =
    root.ticketRepositoryCollection.findOrCreateChild(name)
  def getMilestone(name: String) =
    root.milestoneCollection.findOrCreateChild(name)
  def getVersion(name: String) = 
    root.versionCollection.findOrCreateChild(name)
  def getType(name: String) = 
    root.typeCollection.findOrCreateChild(name)

  def getComponent(name: String) = 
    root.componentCollection.findOrCreateChild(name)
  def getStatus(name: String) =
    root.statusCollection.findOrCreateChild(name)
  def getPerson(name: String) = 
    root.personCollection.findOrCreateChild(name)
  
  def getCommitRepository(name: String) =
    root.commitRepositoryCollection.findOrCreateChild(name)
    
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