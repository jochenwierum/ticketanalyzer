package de.jowisoftware.mining.importer
import scala.actors.Actor

object ImportEvents {
  abstract sealed class ImportEvent
  case class CountedTicket(count: Int) extends ImportEvent
  case class CountedCommits(count: Int) extends ImportEvent
  case class LoadedTicket(ticket: Map[String, Any]) extends ImportEvent
  case class LoadedCommit(commit: Map[String, Any]) extends ImportEvent
  case class Finish() extends ImportEvent
}

abstract class Importer extends Thread {
  private var events: Actor = _
  
  private[importer] def executeAsync(events: Actor) = {
    this.events = events
    start()
  }
  
  final override def run(): Unit = {
    try {
      importAll(events)
    } finally {
      events ! ImportEvents.Finish()
    }
  }
  
  protected def importAll(events: Actor): Unit
}