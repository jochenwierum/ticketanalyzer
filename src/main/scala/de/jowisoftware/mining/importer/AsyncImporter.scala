package de.jowisoftware.mining.importer

import scala.actors.Actor.self

import de.jowisoftware.mining.model.RootNode
import grizzled.slf4j.Logging

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
    val tp = if (ticketsCount == 0) 0 else 1000 * ticketsDone / ticketsCount
    val cp = if (commitsCount == 0) 0 else 1000 * commitsDone / commitsCount
    val total = if (ticketsCount + commitsCount == 0) 0
      else 1000 * (ticketsDone + commitsDone) / (commitsCount + ticketsCount) 
    
    if (lastTotal != total) {
      println(mkStatusLine(tp, cp, total))
      lastTotal = total
    }
  }
  
  private def mkStatusLine(tp: Long, cp: Long, total: Long) =
    "%.1f %% done: %d of %s Tickets (%.1f %%), %d of %s Commits (%.1f %%)".
      format(total / 10.0, ticketsDone, num(ticketsCount), tp / 10.0,
          commitsDone, num(commitsCount), cp / 10.0);

  private def num(x: Long) =
    if (x <= 0) "?"
    else x.toString
}

abstract class AsyncDatabaseImportHandler(root: RootNode, importer: Importer*)
    extends ImportEvents with Logging {
  abstract sealed class ImportEvent
  case class CountedTickets(count: Long) extends ImportEvent
  case class CountedCommits(count: Long) extends ImportEvent
  case class LoadedTicket(ticket: TicketData) extends ImportEvent
  case class LoadedCommit(commit: CommitData) extends ImportEvent
  case object Finish extends ImportEvent
  
  private val target = self
  protected var ticketsDone: Long = 0
  protected var ticketsCount: Long = 0
  protected var commitsDone: Long = 0
  protected var commitsCount: Long = 0
  
  def reportProgress: Unit
  
  def run() = {
    val dbImporter = new DatabaseImportHandler(root)
    var toFinish = importer.size
    
    for (imp <- importer) {
      new AsyncImporterThread(imp).executeAsync(this)
    }
    
    while(toFinish > 0) {
      try {
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
      } catch {
        case e: Exception =>
          error("Unexpected exception while importing, trying to ignore", e)
      }
    }
  }
  
  def countedTickets(count: Long) = target ! CountedTickets(count)
  def countedCommits(count: Long) = target ! CountedCommits(count)
  def loadedTicket(ticket: TicketData) = target ! LoadedTicket(ticket)
  def loadedCommit(commit: CommitData) = target ! LoadedCommit(commit)
  def finish() = target ! Finish
}
