package de.jowisoftware.mining.importer.svn

import scala.swing.{ TextField, PasswordField, Panel, Label, GridPanel }
import de.jowisoftware.mining.importer.ImporterOptions
import scala.swing.event.KeyPressed

class SVNOptions extends ImporterOptions {
  protected var result = Map(("url" -> "https://jowisoftware.de:4443/svn/ssh"),
    ("username" -> ""), ("password" -> ""), ("repositoryname" -> "default"))

  def getPanel(): Panel = new GridPanel(4, 2) {
    contents += label("Url")
    contents += text("url")

    contents += label("User")
    contents += text("username")

    contents += label("Password")
    contents += password("password")

    contents += label("Repository name")
    contents += text("repositoryname")
  }
}