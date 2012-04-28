package de.jowisoftware.mining.importer

import de.jowisoftware.mining.UserOptions

trait Importer {
  def importAll(config: Map[String, String], events: ImportEvents): Unit
  def userOptions: UserOptions
}
