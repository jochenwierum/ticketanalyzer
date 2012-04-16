package de.jowisoftware.mining.importer.trac

import scala.swing.{ TextField, PasswordField, Panel, Label, GridPanel }
import de.jowisoftware.mining.importer.ImporterOptions
import scala.swing.event.KeyPressed

class TracOptions extends ImporterOptions {
  var result: Map[String, String] = Map(
    ("url" -> ""), ("username" -> ""), ("password" -> ""), ("repositoryname" -> "default"))

  def getUserInput: Map[String, String] = result

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

  private def label(text: String) = new Label(text+":")
  private def text(name: String) = initField(new TextField, name)
  private def password(name: String) = initField(new PasswordField, name)

  private def initField(field: TextField, name: String) = {
    field.keys.reactions += {
      case KeyPressed(_, _, _, _) => result += (name -> field.text)
    }

    field.text = result(name)

    field
  }
}