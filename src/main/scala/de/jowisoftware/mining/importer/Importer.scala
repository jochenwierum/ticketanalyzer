package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model.{RootNode, ReportedBy, Owns, InVersion, InMilestone, InComponent, HasType, HasStatus, File, Contains, CommitRepository, ChangedFile}

class DatabaseImportHandler(root: RootNode) extends ImportEvents {
  def finish() { }
  def countedTickets(count: Long) { }
  def countedCommits(count: Long) { }
  
  def loadedTicket(ticketData: Map[String, Any]) = {
    val repository = getTicketRepository(ticketData("repository").toString)
    
    val ticket = repository.createTicket()
    ticket.id(ticketData("id").toString)
    ticket.title(ticketData("summary").toString)
    ticket.text(ticketData("description").toString)
    
    ticket.add(getPerson(ticketData("reporter").toString))(ReportedBy)
    ticket.add(getMilestone(ticketData("milestone").toString()))(InMilestone)
    ticket.add(getVersion(ticketData("version").toString))(InVersion)
    ticket.add(getType(ticketData("type").toString))(HasType)
    ticket.add(getComponent(ticketData("component").toString))(InComponent)
    ticket.add(getStatus(ticketData("status").toString))(HasStatus)
    ticket.add(getPerson(ticketData("owner").toString))(Owns)
    
    repository.add(ticket)(Contains)
  }
  
  def loadedCommit(commitData: Map[String, Any]) = {
    val repository = getCommitRepository(commitData("repository").toString)

    val commit = repository.createCommit()
    commit.id(commitData("id").toString)
    commit.date(commitData("date").toString)
    commit.message(commitData("message").toString)
    
    commit.add(getPerson(commitData("author").toString))(Owns)
    
    val pathes: Map[String, String] =
      commitData("pathes").asInstanceOf[Map[String, String]]
    
    pathes.foreach{case (filename, value) =>
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