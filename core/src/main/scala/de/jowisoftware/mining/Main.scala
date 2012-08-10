package de.jowisoftware.mining

import scala.swing.{Dialog, Swing}

import org.slf4j.bridge.SLF4JBridgeHandler

import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.plugins.{PluginManager, PluginScanner, PluginType}
import de.jowisoftware.neo4j.database.{EmbeddedDatabase, EmbeddedDatabaseWithConsole}
import de.jowisoftware.util.AppUtil
import grizzled.slf4j.Logging
import gui.MainWindow
import javax.swing.UIManager

object Main extends Logging {
  lazy val compactMode = try {
    Class.forName("org.neo4j.server.WrappingNeoServerBootstrapper")
    false
  } catch {
    case _ => true
  }

  def main(args: Array[String]) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch {
      case e: Exception =>
    }

    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    Swing.onEDT {
      val pluginManager = preparePluginManager
      checkPlugins(pluginManager)
      val db = openDatabase

      new MainWindow(db, pluginManager).visible = true
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
      Dialog.showMessage(null, "No plugins found", "Critical Error", Dialog.Message.Error, null)
      System.exit(1)
    }
  }

  private def openDatabase: EmbeddedDatabase[RootNode] = {
    val dbPath = AppUtil.projectFile(AppUtil.appSettings.getString("db"))
    info("Using database at "+dbPath)
    if (compactMode)
      new EmbeddedDatabase(dbPath, RootNode)
    else
      new EmbeddedDatabaseWithConsole(dbPath, RootNode)
  }
}
