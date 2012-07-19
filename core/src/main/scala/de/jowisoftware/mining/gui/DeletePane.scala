package de.jowisoftware.mining.gui

import scala.swing.{ Orientation, Label, Frame, Button, BoxPanel, Alignment }
import scala.swing.event.ButtonClicked

import de.jowisoftware.mining.gui.MainWindow.DatabaseUpdated
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database

class DeletePane(val db: Database[RootNode], parent: Frame)
    extends BoxPanel(Orientation.Vertical) with GuiTab {

  val label = new Label("""<html><p>
      |<b>Drop the database</b><br />
      |By clicking on &quot;yes, drop the database&quot; the whole database (the directory<br />
      |&quot;db&quot; in the program installation) will be deleted. There is <i>no</i> way back!<br />
      |You can backup the directory by simply copying it while the application<br />
      |is not running.<br /><br />
      |Are you shure you want to delete the database?
      |</p></html>""".stripMargin)
  label.horizontalAlignment = Alignment.Left

  val button = new Button("Yes, drop the database")
  contents += label
  contents += button

  listenTo(button)

  reactions += {
    case ButtonClicked(`button`) => dropDB()
  }

  def dropDB() {
    db.deleteContent
    parent.publish(DatabaseUpdated)
  }

  def align = {}
}