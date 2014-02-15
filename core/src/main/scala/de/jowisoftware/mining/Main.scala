package de.jowisoftware.mining

import scala.swing.{ Dialog, Swing }
import org.slf4j.bridge.SLF4JBridgeHandler
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.plugins.{ PluginManager, PluginScanner, PluginType }
import de.jowisoftware.neo4j.database.{ EmbeddedDatabase, EmbeddedDatabaseWithConsole }
import de.jowisoftware.util.AppUtil
import grizzled.slf4j.Logging
import gui.MainWindow
import javax.swing.UIManager
import de.jowisoftware.neo4j.Database

object Main extends Logging {
  lazy val compactMode = try {
    Class.forName("org.neo4j.server.database.CommunityDatabase")
    false
  } catch {
    case _: Exception => true
  }

  def main(args: Array[String]): Unit = {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch {
      case e: Exception =>
    }

    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    performDBUpdate()
    Swing.onEDT(startApp)
  }

  private def startApp {
    val db = openDatabase

    val pluginManager = preparePluginManager
    checkPlugins(pluginManager)

    val status = try {
      new MainWindow(db, pluginManager).visible = true
      0
    } catch {
      case e: Exception =>
        error("Failed to initialize the application", e)
        1
    } finally {
      try {
        db.shutdown
        AkkaHelper.system.shutdown()
      } catch {
        case _: Exception =>
      }
    }

    System.exit(status)
  }

  private def performDBUpdate() {
    warn("Cheking whether database upgrade is required")

    val db = openDatabase
    val hasUpdates = try {
      db.inTransaction { _.rootNode(RootNode).updateRequired }
    } finally {
      db.shutdown
    }

    if (hasUpdates) {
      warn("Performing update")
      UpdateDB.main(Array())
    } else {
      warn("Database version is already up to date")
    }
  }

  private def preparePluginManager = {
    val pluginDirs = AppUtil.appSettings.getArray("plugindirs")
    val pluginManager = new PluginManager()
    val scanner = new PluginScanner(AppUtil.basePath, pluginDirs: _*)
    scanner.scan(pluginManager)
    pluginManager
  }

  private def checkPlugins(pluginManager: PluginManager) {
    if (!PluginType.values.forall(pluginManager.getFor(_).size > 0)) {
      Dialog.showMessage(null, "No plugins found", "Critical Error", Dialog.Message.Error)
      System.exit(1)
    }
  }

  private def openDatabase: Database = {
    val dbPath = AppUtil.projectFile(AppUtil.appSettings.getString("db"))

    info("Using database at "+dbPath)
    try {
      if (compactMode)
        new EmbeddedDatabase(dbPath)
      else
        new EmbeddedDatabaseWithConsole(dbPath)
    } catch {
      case e: IllegalStateException =>
        Dialog.showMessage(null, "The database is locked - is another instance of this program running?",
          "Critical Error", Dialog.Message.Error)
        System.exit(1)
        null
    }
  }
}
