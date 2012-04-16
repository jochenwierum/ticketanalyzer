package de.jowisoftware.mining.importer

import scala.swing.Panel
import scala.swing.PasswordField
import scala.swing.Label
import scala.swing.TextField
import scala.swing.event.KeyPressed

trait ImporterOptions {
  protected var result: Map[String, String]
  def getUserInput: Map[String, String] = result

  protected def label(text: String) = new Label(text+":")

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

  def getPanel(): Panel
}