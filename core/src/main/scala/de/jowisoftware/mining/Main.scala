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

object Main {
  def main(args: Array[String]) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch {
      case e: Exception =>
    }

    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    Swing.onEDT {
      val dbPath = System.getProperty("dbpath", "db/")
      val db = EmbeddedDatabase(dbPath, RootNode)
      val pluginManager = preparePluginManager

      new MainWindow(db, pluginManager).visible = true
    }
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
