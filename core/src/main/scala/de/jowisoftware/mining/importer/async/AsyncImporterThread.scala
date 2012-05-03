package de.jowisoftware.mining.importer.async
import de.jowisoftware.mining.importer.ImportEvents
import de.jowisoftware.mining.importer.Importer
import de.jowisoftware.mining.importer.TicketData
import de.jowisoftware.mining.importer.CommitData

class AsyncImporterThread(config: Map[String, String], importer: Importer) extends Thread {
  private var events: ImportEvents = _

  private class EventWrapper(inner: ImportEvents) extends ImportEvents {
    private var finished = false
    def countedTickets(count: Long) = inner.countedTickets(count)
    def countedCommits(count: Long) = inner.countedCommits(count)
    def loadedTicket(ticket: TicketData) = inner.loadedTicket(ticket)
    def loadedCommit(commit: CommitData) = inner.loadedCommit(commit)
    def finish() {
      if (!finished) {
        finished = true
        inner.finish()
      }
    }
  }

  private[importer] def executeAsync(events: ImportEvents) = {
    this.events = new EventWrapper(events)
    start()
  }

  final override def run(): Unit = {
    try {
      importer.importAll(config, events)
    } catch {
      case e: Exception => events.finish
    }
  }
}