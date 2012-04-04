package de.jowisoftware.mining.importer

import scala.actors.Actor.self

import de.jowisoftware.mining.model.RootNode

class AsyncImporterThread(importer: Importer) extends Thread {
  private var events: ImportEvents = _
  
  private[importer] def executeAsync(events: ImportEvents) = {
    this.events = events
    start()
  }
  
  final override def run(): Unit = {
    importer.importAll(events)
  }
}

trait ConsoleProgressReporter extends AsyncDatabaseImportHandler {
  private var lastTotal = -1L
  
  def reportProgress {
    val tp = 1000 * ticketsDone / ticketsCount
    val cp = 1000 * commitsDone / commitsCount
    val total = 1000 * (ticketsDone + commitsDone) / (commitsCount + ticketsCount) 
    
    if (lastTotal != total) {
      println(mkStatusLine(tp, cp, total))
      lastTotal = total
    }
  }
  
  private def mkStatusLine(tp: Long, cp: Long, total: Long) =
    "%.1f %% done: %d of %d Tickets (%.1f %%), %d of %d Commits (%.1f %%)".
      format(total / 10.0, ticketsDone, ticketsCount, tp / 10.0,
          commitsDone, commitsCount, cp / 10.0);
}

abstract class AsyncDatabaseImportHandler(root: RootNode, importer: Importer*) extends ImportEvents {
  abstract sealed class ImportEvent
  case class CountedTickets(count: Long) extends ImportEvent
  case class CountedCommits(count: Long) extends ImportEvent
  case class LoadedTicket(ticket: Map[String, Any]) extends ImportEvent
  case class LoadedCommit(commit: Map[String, Any]) extends ImportEvent
  case object Finish extends ImportEvent
  
  private val target = self
  protected var ticketsDone: Long = 0
  protected var ticketsCount: Long = -1
  protected var commitsDone: Long = 0
  protected var commitsCount: Long = -1
  
  def reportProgress: Unit
  
  def run() = {
    val dbImporter = new DatabaseImportHandler(root)
    var toFinish = importer.size
    
    for (imp <- importer) {
      new AsyncImporterThread(imp).executeAsync(this)
    }
    
    while(toFinish > 0) {
      self.receive {
        case CountedCommits(c) => 
          dbImporter.countedCommits(c)
          commitsCount = c
          reportProgress
        case CountedTickets(t) =>
          dbImporter.countedTickets(t)
          ticketsCount = t
          reportProgress
        case LoadedTicket(data) =>
          ticketsDone += 1
          dbImporter.loadedTicket(data)
          reportProgress
        case LoadedCommit(data) =>
          commitsDone += 1
          dbImporter.loadedCommit(data)
          reportProgress
        case Finish =>
          toFinish = toFinish - 1
      }
    }
  }
  
  def countedTickets(count: Long) = target ! CountedTickets(count)
  def countedCommits(count: Long) = target ! CountedCommits(count)
  def loadedTicket(ticket: Map[String, Any]) = target ! LoadedTicket(ticket)
  def loadedCommit(commit: Map[String, Any]) = target ! LoadedCommit(commit)
  def finish() = target ! Finish
}
