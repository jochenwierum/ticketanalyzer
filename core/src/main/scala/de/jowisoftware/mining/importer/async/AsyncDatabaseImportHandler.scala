package de.jowisoftware.mining.importer.async

import scala.actors.Actor.self
import de.jowisoftware.mining.model.RootNode
import grizzled.slf4j.Logging
import de.jowisoftware.mining.importer.TicketData
import de.jowisoftware.mining.importer.Importer
import de.jowisoftware.mining.importer.ImportEvents
import de.jowisoftware.mining.importer.CommitData
import de.jowisoftware.mining.importer.DatabaseImportHandler
import de.jowisoftware.mining.importer.TicketCommentData
import de.jowisoftware.neo4j.DBWithTransaction

class AsyncDatabaseImportHandler(
  db: DBWithTransaction[RootNode],
  importer: (Importer, Map[String, String])*)
    extends ImportEvents with Logging {

  abstract sealed class ImportEvent
  case class CountedTickets(count: Long) extends ImportEvent
  case class CountedCommits(count: Long) extends ImportEvent
  case class LoadedTicket(repository: String, tickets: List[TicketData], comments: Seq[TicketCommentData]) extends ImportEvent
  case class LoadedCommit(repository: String, commit: CommitData) extends ImportEvent
  case object Finish extends ImportEvent

  private val target = self
  private var ticketsDone: Long = 0
  private var ticketsCount: Long = 0
  private var commitsDone: Long = 0
  private var commitsCount: Long = 0

  def reportProgress(ticketsDone: Long, ticketsCount: Long, commitsDone: Long, commitsCount: Long) {}

  private def reportProgress() {
    reportProgress(ticketsDone, ticketsCount, commitsDone, commitsCount)
  }

  def run() = {
    val dbImporter = new DatabaseImportHandler(db)
    var toFinish = importer.size

    for ((imp, config) <- importer) {
      new AsyncImporterThread(config, imp).executeAsync(this)
    }

    while (toFinish > 0) {
      try {
        self.receive {
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
          case Finish =>
            toFinish = toFinish - 1
            if (toFinish == 0) {
              dbImporter.finish
            }
        }
      } catch {
        case e: Exception =>
          error("Unexpected exception while importing, trying to ignore", e)
      }
    }
  }

  def countedTickets(count: Long) = target ! CountedTickets(count)
  def countedCommits(count: Long) = target ! CountedCommits(count)
  def finish() = target ! Finish

  def loadedTicket(repository: String, tickets: List[TicketData], comments: Seq[TicketCommentData]) =
    target ! LoadedTicket(repository, tickets, comments)

  def loadedCommit(repository: String, commit: CommitData) =
    target ! LoadedCommit(repository, commit)
}