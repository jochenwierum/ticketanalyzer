package de.jowisoftware.mining.importer.async

import akka.actor._

import de.jowisoftware.mining.importer.{ TicketData, TicketCommentData, Importer, ImportEvents, DatabaseImportHandler, CommitData }
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.{ Database, DBWithTransaction }
import grizzled.slf4j.Logging

private[async] abstract class ImportEvent
private[async] case class CountedTickets(count: Long) extends ImportEvent
private[async] case class CountedCommits(count: Long) extends ImportEvent
private[async] case class LoadedTicket(repository: String, tickets: List[TicketData], comments: Seq[TicketCommentData]) extends ImportEvent
private[async] case class LoadedCommit(repository: String, commit: CommitData) extends ImportEvent
private[async] case class SetupCommits(supportsAbbrev: Boolean) extends ImportEvent

private[async] case object Finish extends ImportEvent
private[async] case class Run(db: Database,
  importer: (Importer, Map[String, String])*) extends ImportEvent

class AsyncDatabaseImportHandler extends Actor with ImportEvents with Logging {

  private val target = self
  private var ticketsDone: Long = 0
  private var ticketsCount: Long = 0
  private var commitsDone: Long = 0
  private var commitsCount: Long = 0
  private var toFinish: Long = 0
  private var dbImporter: DatabaseImportHandler = _

  def reportProgress(ticketsDone: Long, ticketsCount: Long, commitsDone: Long, commitsCount: Long) {}

  private def reportProgress() {
    reportProgress(ticketsDone, ticketsCount, commitsDone, commitsCount)
  }

  def run(db: Database,
    importer: (Importer, Map[String, String])*): Unit = {
    toFinish = importer.size
    dbImporter = new DatabaseImportHandler(db)

    for ((imp, config) <- importer) {
      new AsyncImporterThread(config, imp).executeAsync(this)
    }
  }

  def receive: PartialFunction[Any, Unit] = {
    case Run(db, importer) =>
      run(db, importer)
    case CountedCommits(c) =>
      dbImporter.countedCommits(c)
      commitsCount += c
      reportProgress
    case CountedTickets(t) =>
      dbImporter.countedTickets(t)
      ticketsCount += t
      reportProgress
    case LoadedTicket(repository, tickets, comments) =>
      ticketsDone += 1
      dbImporter.loadedTicket(repository, tickets, comments)
      reportProgress
    case LoadedCommit(repository, data) =>
      commitsDone += 1
      dbImporter.loadedCommit(repository, data)
      reportProgress
    case SetupCommits(supportsAbbrev) =>
      dbImporter.setupCommits(supportsAbbrev)
    case Finish =>
      toFinish = toFinish - 1
      if (toFinish == 0) {
        dbImporter.finish
        context.stop(self)
      }
  }

  def countedTickets(count: Long) = target ! CountedTickets(count)
  def countedCommits(count: Long) = target ! CountedCommits(count)
  def finish() = target ! Finish

  def loadedTicket(repository: String, tickets: List[TicketData], comments: Seq[TicketCommentData]) =
    target ! LoadedTicket(repository, tickets, comments)

  def loadedCommit(repository: String, commit: CommitData) =
    target ! LoadedCommit(repository, commit)

  def setupCommits(supportsAbbrev: Boolean) =
    target ! SetupCommits(supportsAbbrev)
}