package de.jowisoftware.mining.importer.mantis

import de.jowisoftware.mining.importer.Importer
import scala.collection.immutable.Map
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.importer.ImportEvents

class MantisImporterFacade extends Importer {
  def importAll(config: Map[String, String], events: ImportEvents) =
    new MantisImporter(config, events).run()

  def userOptions(): UserOptions = new MantisOptions()
}