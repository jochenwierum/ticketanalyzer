package de.jowisoftware.mining

import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.database.EmbeddedDatabase
import de.jowisoftware.mining.plugins._
import javax.swing.SwingUtilities
import gui.MainWindow
import de.jowisoftware.mining.settings.Settings
import java.io.File
import javax.swing.UIManager
import scala.swing.Swing
import org.slf4j.bridge.SLF4JBridgeHandler
import scala.swing.Dialog
import de.jowisoftware.neo4j.database.EmbeddedDatabaseWithConsole
import de.jowisoftware.util.AppUtil

object Main {
  val settings = new Settings("config.properties")

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
    val pluginDirs = settings.getArray("plugindirs")
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
    val dbPath = new File(AppUtil.basePath, settings.getString("db")).getCanonicalFile
    if (compactMode)
      new EmbeddedDatabase(dbPath, RootNode)
    else
      new EmbeddedDatabaseWithConsole(dbPath, RootNode)
  }
}
