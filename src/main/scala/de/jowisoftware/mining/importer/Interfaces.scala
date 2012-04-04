package de.jowisoftware.mining.importer
import scala.actors.Actor

trait ImportEvents {
  def countedTickets(count: Long)
  def countedCommits(count: Long)
  def loadedTicket(ticket: Map[String, Any])
  def loadedCommit(commit: Map[String, Any])
  def finish()
}

trait Importer {
  def importAll(events: ImportEvents): Unit
}
