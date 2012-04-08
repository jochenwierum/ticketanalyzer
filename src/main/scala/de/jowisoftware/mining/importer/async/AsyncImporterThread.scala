package de.jowisoftware.mining.importer.async
import de.jowisoftware.mining.importer.ImportEvents
import de.jowisoftware.mining.importer.Importer

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