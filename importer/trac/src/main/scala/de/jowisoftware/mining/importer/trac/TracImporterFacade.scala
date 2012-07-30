package de.jowisoftware.mining.importer.trac

import de.jowisoftware.mining.importer.Importer
import de.jowisoftware.mining.importer.ImportEvents

class TracImporterFacade extends Importer {
  def userOptions = new TracOptions()

  def importAll(config: Map[String, String], events: ImportEvents) =
    new TracImporter(config, events).run()
}