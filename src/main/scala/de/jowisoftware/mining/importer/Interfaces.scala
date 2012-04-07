package de.jowisoftware.mining.importer
import scala.actors.Actor
import java.util.Date

case class TicketUpdate(id: Int, field: String, newvalue: String,
  oldvalue: String="", author: String="", time: Date=new Date())

case class TicketData(repository: String, id: Int,
  summary: String="", description: String="",
  creationDate: Date=new Date(),
  updateDate: Date=new Date(), tags: String="",
  reporter: String="", version: String="", ticketType: String="",
  milestone: String="", component: String="", status: String="", owner: String="",
  resolution: String="", blocking: String="", priority: String="",
  updates: List[TicketUpdate]=List())

case class CommitData(repository: String, id: String,
  author: String="", message: String="", date: Date = new Date(),
  files: Map[String, String]=Map())
    
trait ImportEvents {
  def countedTickets(count: Long)
  def countedCommits(count: Long)
  def loadedTicket(ticket: TicketData)
  def loadedCommit(commit: CommitData)
  def finish()
}

trait Importer {
  def importAll(events: ImportEvents): Unit
}
