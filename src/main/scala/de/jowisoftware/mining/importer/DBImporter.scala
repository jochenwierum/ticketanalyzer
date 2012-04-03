package de.jowisoftware.mining.importer.trac
import de.jowisoftware.neo4j.DefaultTransaction
import de.jowisoftware.mining.model._
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.mining.importer.{ImportEvents, Importer}
import de.jowisoftware.mining.importer.ImportEvents._
import scala.actors.Actor._
import scala.actors.Actor

class DBImporter(root: RootNode, importer: Importer*) {
  def run() = {
    var toFinish = importer.size
    
    for (imp <- importer) {
      imp.executeAsync(self)
    }
    
    while(toFinish > 0) {
      self.receive {
        case CountedCommits(c) => println("Commits: "+ c)
        case CountedTicket(t) => println("Tickets: "+ t)
        case LoadedTicket(data) => print("t"); loadedTicket(data)
        case LoadedCommit(data) => print("c"); loadedCommit(data)
        case Finish() => 
          print("F")
          toFinish = toFinish - 1
      }
    }
  }
  
  private def loadedTicket(ticketData: Map[String, Any]) = {
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