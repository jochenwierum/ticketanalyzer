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

    val pluginDirs = new Settings().getArray("plugindirs")
    val pluginDirsAsFile = pluginDirs.map(new File(_))
    val pluginManager = new PluginManager()
    val scanner = new PluginScanner(pluginDirsAsFile: _*)
    scanner.scan(pluginManager)

    //new MainWindow(pluginManager).visible = true
    //new Assistant("test", new TestPage(), new TestPage(), new TestPage()).visible = true
    new ImportAssistant().run(pluginManager)

    println("Done")
    /*
    val dbPath = "db/"
    Database.drop(dbPath)
    val db = Database(dbPath, RootNode)

    try {
      db.inTransaction {
        trans: DBWithTransaction[RootNode] =>
          importFull(trans)
          trans.success
      }
    } finally {
      db.shutdown;
    }

    println("done")
    scala.actors.Scheduler.shutdown()
    */
  }

  def importFull(db: DBWithTransaction[RootNode]) = {
    val importer = new AsyncDatabaseImportHandler(db.rootNode,
      importSVN(db),
      importTrac(db)) with ConsoleProgressReporter
    importer.run()
  }

  def importTrac(db: DBWithTransaction[RootNode]) = {
    val importer = Class.forName("de.jowisoftware.mining.importer.trac.TracImporter").newInstance()
    val config = Map(
      "url" -> "http://jowisoftware.de/trac/ssh/login/xmlrpc",
      "username" -> "test",
      "password" -> "test",
      "repositoryName" -> "trac1")
    importer.asInstanceOf[Importer] -> config
  }

  def importSVN(db: DBWithTransaction[RootNode]) = {
    val importer = Class.forName("de.jowisoftware.mining.importer.svn.SVNImporter").newInstance()
    val config = Map("url" -> "https://test@jowisoftware.de:4443/svn/ssh",
      "repositoryName" -> "svn1")
    importer.asInstanceOf[Importer] -> config
  }
}
