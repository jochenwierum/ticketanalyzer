package de.jowisoftware.mining.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Orientation
import de.jowisoftware.neo4j.Database
import scala.swing.event.ButtonClicked
import scala.swing.Button
import scala.swing.Frame
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.gui.MainWindow.DatabaseUpdated

class DeletePane(val db: Database[RootNode], parent: Frame) extends BoxPanel(Orientation.Vertical) {
  val button = new Button("Drop database")
  contents += button

  listenTo(button)

  reactions += {
    case ButtonClicked(`button`) => dropDB()
  }

  def dropDB() {
    db.deleteContent
    parent.publish(DatabaseUpdated)
  }
}