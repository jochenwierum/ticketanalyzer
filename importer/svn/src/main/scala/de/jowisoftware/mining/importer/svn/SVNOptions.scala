package de.jowisoftware.mining.importer.svn

import scala.swing.{ TextField, PasswordField, Panel, Label, GridPanel }
import de.jowisoftware.mining.UserOptions
import scala.swing.event.KeyPressed

class SVNOptions extends UserOptions {
  protected val defaultResult = Map(
      "url" -> "https://jowisoftware.de:4443/svn/ssh",
      "username" -> "",
      "password" -> "",
      "repositoryname" -> "default")

  protected val htmlDescription = """<p><b>SVN Importer</b></p>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Url", text("url"))
    panel.add("User", text("username"))
    panel.add("Password", password("password"))
    panel.add("Repository name", text("repositoryname"))
  }
}