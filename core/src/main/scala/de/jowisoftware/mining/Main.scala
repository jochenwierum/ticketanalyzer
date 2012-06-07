package de.jowisoftware.mining

import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.neo4j.EmbeddedDatabase
import de.jowisoftware.mining.plugins._
import javax.swing.SwingUtilities
import gui.MainWindow
import de.jowisoftware.mining.settings.Settings
import java.io.File
import javax.swing.UIManager
import scala.swing.Swing
import org.slf4j.bridge.SLF4JBridgeHandler
import scala.swing.Dialog

object Main {
  val basePath = new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
  val settings = new Settings("config.properties")

  def main(args: Array[String]) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch {
      case e: Exception =>
    }

    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    Swing.onEDT {
      val dbPath = new File(basePath, settings.getString("db")).getCanonicalFile
      val pluginManager = preparePluginManager
      checkPlugins(pluginManager)

      val db = EmbeddedDatabase(dbPath, RootNode)

      new MainWindow(db, pluginManager).visible = true
    }
  }

  private def preparePluginManager = {
    val pluginDirs = settings.getArray("plugindirs")
    val pluginManager = new PluginManager()
    val scanner = new PluginScanner(basePath, pluginDirs: _*)
    scanner.scan(pluginManager)
    pluginManager
  }

  private def checkPlugins(pluginManager: PluginManager) {
    if (!PluginType.values.forall(pluginManager.getFor(_).size > 0)) {
      Dialog.showMessage(null, "No plugins found", "Critical Error", Dialog.Message.Error, null)
      System.exit(1)
    }
  }
}
