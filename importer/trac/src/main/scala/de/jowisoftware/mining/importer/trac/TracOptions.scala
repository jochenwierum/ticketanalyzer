package de.jowisoftware.mining.importer.trac

import scala.swing.{ TextField, PasswordField, Panel, Label, GridPanel }
import de.jowisoftware.mining.UserOptions
import scala.swing.event.KeyPressed

class TracOptions extends UserOptions {
  protected var result: Map[String, String] = Map(
    ("url" -> "http://jowisoftware.de/trac/test/login/xmlrpc"),
    ("username" -> "test"), ("password" -> "test"), ("repositoryname" -> "default"))

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