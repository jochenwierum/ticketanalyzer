package de.jowisoftware.mining

import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.plugins._
import javax.swing.SwingUtilities
import gui.MainWindow
import de.jowisoftware.mining.settings.Settings
import java.io.File

object Main {
  def main(args: Array[String]) {
    SwingUtilities.invokeLater(new Runnable() {
      def run = {
        val dbPath = "db/"
        val db = Database(dbPath, RootNode)
        val pluginManager = preparePluginManager

        new MainWindow(db, pluginManager).visible = true
      }
    })
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
