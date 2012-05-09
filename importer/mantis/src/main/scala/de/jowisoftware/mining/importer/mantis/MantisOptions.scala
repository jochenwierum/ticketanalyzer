package de.jowisoftware.mining.importer.mantis

import scala.swing.{ TextField, PasswordField, Panel, Label, GridPanel }
import de.jowisoftware.mining.UserOptions
import scala.swing.event.KeyPressed
import scala.swing.GridBagPanel
import scala.swing.Component

class MantisOptions extends UserOptions {
  protected var result: Map[String, String] = Map(
    ("url" -> "http://jowisoftware.de/mant/api/soap/mantisconnect.php"),
    ("username" -> "administrator"), ("password" -> "test"), ("repositoryname" -> "default"),
    ("project" -> "1"))

  def getPanel(): Panel = new GridBagPanel {
    var line = 0
    def c(c1: Component, c2: Component) = {
      val c = new Constraints
      c.fill = GridBagPanel.Fill.Horizontal
      c.gridx = 0
      c.gridy = line
      layout(c1) = c
      c.gridx = 1
      layout(c2) = c
      line = line + 1
    }

    def fillToBottom() {
      val c = new Constraints
      c.fill = GridBagPanel.Fill.Vertical
      c.gridy = line
      c.gridx = 0
      layout(new Label("")) = c
    }

    c(label("Url"), text("url"))
    c(label("Project Id"), text("project"))
    c(label("User"), text("username"))
    c(label("Password"), password("password"))
    c(label("Repository name"), text("repositoryname"))

    fillToBottom()
  }
}