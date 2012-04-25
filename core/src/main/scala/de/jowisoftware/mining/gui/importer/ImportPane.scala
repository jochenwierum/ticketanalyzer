package de.jowisoftware.mining.gui.importer

import scala.swing.BoxPanel
import scala.swing.Label
import scala.swing.Button
import scala.swing.event.ButtonClicked
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.importer.async.ConsoleProgressReporter
import de.jowisoftware.mining.importer.Importer
import de.jowisoftware.mining.importer.async.AsyncDatabaseImportHandler
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.mining.plugins.PluginManager
import de.jowisoftware.mining.model.RootNode
import scala.swing.Orientation
import scala.swing.Frame

import de.jowisoftware.mining.gui.MainWindow.DatabaseUpdated

class ImportPane(db: Database[RootNode], pluginManager: PluginManager, parent: Frame) extends BoxPanel(Orientation.Vertical) {
  val button = new Button("Import")
  contents += new Label("Warning! (Re-)Importing Data wipes the database!")
  contents += button

  listenTo(button)

  reactions += {
    case ButtonClicked(`button`) => runImport()
  }

  def runImport() {
    val plugins = new ImportAssistant(pluginManager).show(parent)

    plugins match {
      case Some(plugins) =>
        db.deleteContent
        db.inTransaction { trans: DBWithTransaction[RootNode] =>
          importFull(trans, plugins)
          trans.success
        }
        parent.publish(DatabaseUpdated)
      case None =>
    }
  }

  def importFull(db: DBWithTransaction[RootNode], plugins: List[(Importer, Map[String, String])]) = {
    val importer = new AsyncDatabaseImportHandler(db.rootNode, plugins.toArray: _*) with ConsoleProgressReporter
    importer.run()
    db.rootNode.state(1)
  }
}