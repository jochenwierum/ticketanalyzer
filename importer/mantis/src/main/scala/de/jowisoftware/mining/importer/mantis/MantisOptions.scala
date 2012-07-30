package de.jowisoftware.mining.importer.mantis

import scala.swing.{ TextField, PasswordField, Panel, Label, GridPanel }
import de.jowisoftware.mining.UserOptions
import scala.swing.event.KeyPressed
import scala.swing.GridBagPanel
import scala.swing.Component
import java.awt.Color
import javax.swing.BorderFactory
import scala.swing.Alignment
import java.awt.Insets

class MantisOptions extends UserOptions {
  protected val defaultResult: Map[String, String] = Map(
    "url" -> "http://jowisoftware.de/mant/",
    "username" -> "administrator",
    "password" -> "test",
    "repositoryname" -> "default",
    "project" -> "1",
    "filter" -> "1",
    "dateformat" -> "en")

  protected val htmlDescription =
    """<p><b>Mantis importer</b><br><br>
    This importer reads a mantis repository.<br>
    To use this importer, make sure, that:
    </p>
    <ul>
      <li>Mantis RPC is enabled</li>
      <li>The UI of the user is English</li>
    </ul>
    """

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Url", text("url"))
    panel.add("Project Id", text("project"))
    panel.add("User", text("username"))
    panel.add("Password", password("password"))
    panel.add("Filter", text("filter"))
    panel.add("Date format", combobox("dateformat", Seq("en", "de")))
    panel.add("Repository name", text("repositoryname"))
  }
}