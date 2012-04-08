package de.jowisoftware.mining.importer

trait Importer {
  def importAll(events: ImportEvents): Unit
}
