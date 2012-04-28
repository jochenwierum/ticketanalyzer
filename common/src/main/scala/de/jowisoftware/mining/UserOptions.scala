package de.jowisoftware.mining

import scala.swing.event.KeyPressed
import scala.swing.{TextField, PasswordField, Panel, Label, Alignment}

trait UserOptions {
  protected var result: Map[String, String]

  def getUserInput: Map[String, String] = result
  def getPanel(): Panel

  protected def label(text: String) = {
    val label = new Label(text+":")
    label.xAlignment = Alignment.Left
    label
  }

  protected def text(name: String) = {
    val field = new TextField
    field.keys.reactions += {
      case KeyPressed(_, _, _, _) => result += (name -> field.text)
    }

    field.text = result(name)
    field
  }

  protected def password(name: String) = {
    val field = new PasswordField(result(name))
    field.keys.reactions += {
      case KeyPressed(_, _, _, _) => result += (name -> field.password.mkString(""))
    }
    field
  }
}