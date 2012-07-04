package de.jowisoftware.mining.importer.redmine

import de.jowisoftware.mining.importer.ImportEvents
import de.jowisoftware.mining.importer.Importer

class RedmineImporterFacade extends Importer {
  def userOptions = new RedmineOptions()

  def importAll(config: Map[String, String], events: ImportEvents) =
    new RedmineImporter(config, events).run();
}