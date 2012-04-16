package de.jowisoftware.mining

import de.jowisoftware.mining.importer.Importer
import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.neo4j.{ Database, DBWithTransaction }
import de.jowisoftware.mining.importer.async.AsyncDatabaseImportHandler
import de.jowisoftware.mining.importer.async.ConsoleProgressReporter
import java.io.File
import de.jowisoftware.mining.plugins._
import de.jowisoftware.mining.settings.Settings
import de.jowisoftware.mining.awt._
import de.jowisoftware.mining.awt.importer._

object Main {
  def main(args: Array[String]) {
    val pluginManager = preparePluginManager
    val plugins = new ImportAssistant().show(pluginManager)

    val dbPath = "db/"
    Database.drop(dbPath)
    val db = Database(dbPath, RootNode)

    try {
      db.inTransaction {
        trans: DBWithTransaction[RootNode] =>
          importFull(trans, plugins)
          trans.success
      }
    } finally {
      db.shutdown;
    }

    scala.actors.Scheduler.shutdown()
  }

  def importFull(db: DBWithTransaction[RootNode], plugins: List[(Importer, Map[String, String])]) = {
    val importer = new AsyncDatabaseImportHandler(db.rootNode, plugins.toArray: _*) with ConsoleProgressReporter
    importer.run()
  }

  private def preparePluginManager = {
    val pluginDirs = new Settings().getArray("plugindirs")
    val pluginDirsAsFile = pluginDirs.map(new File(_))
    val pluginManager = new PluginManager()
    val scanner = new PluginScanner(pluginDirsAsFile: _*)
    scanner.scan(pluginManager)
    pluginManager
  }
}
