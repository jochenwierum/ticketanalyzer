package de.jowisoftware.mining.importer.trac
import de.jowisoftware.mining.importer.ImportEvents
import de.jowisoftware.neo4j.DefaultTransaction
import de.jowisoftware.mining.model._
import de.jowisoftware.neo4j.DBWithTransaction

class DBImporter(db: DBWithTransaction[RootNode], repositoryName: String) extends ImportEvents {
  def loadedTicket(ticketData: Map[String, Any]) = {
    
    val repository = getRepository(repositoryName)
    
    val ticket = db.createNode(Ticket)
    ticket.id(ticketData("id").toString)
    ticket.reporter(ticketData("reporter").toString)
    ticket.title(ticketData("summary").toString)
    ticket.text(ticketData("description").toString)
    ticket.add(getMilestone(ticketData("milestone").toString()))(InMilestone)
    ticket.add(getVersion(ticketData("version").toString))(InVersion)
    ticket.add(getType(ticketData("type").toString))(HasType)
    ticket.add(getComponent(ticketData("component").toString))(InComponent)
    ticket.add(getStatus(ticketData("status").toString))(HasStatus)
    ticket.owner(ticketData("owner").toString) // TODO: own node!
    
    repository.add(ticket)(Contains)
  }
  
  def getRepository(name: String): TicketRepository = TicketRepository.findOrCreate(db, name)
  def getMilestone(name: String): Milestone = Milestone.findOrCreate(db, name)
  def getVersion(name: String): Version = Version.findOrCreate(db, name)
  def getType(name: String): Type = Type.findOrCreate(db, name)
  def getComponent(name: String): Component = Component.findOrCreate(db, name)
  def getStatus(name: String): Status = Status.findOrCreate(db, name) 
}