package de.jowisoftware.mining.importer

trait ImportEvents {
  def loadedTicket(ticket: Map[String, Any]): Unit
}

trait Importer {
  def importAll(events: ImportEvents): Unit
}