package de.jowisoftware.mining.importer

trait Importer {
  def importAll(config: Map[String, String], events: ImportEvents): Unit
}
