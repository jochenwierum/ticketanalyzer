package de.jowisoftware.mining

import java.awt.Insets
import scala.swing._
import scala.swing.event.{ ValueChanged, ButtonClicked }
import scala.swing.event.SelectionChanged

abstract class UserOptions {
  protected class CustomizedGridBagPanel(htmlDescrition: String) extends GridBagPanel {
    var line = 1

    def add(text: String, component: Component) = {
      val constraints = new Constraints
      constraints.gridx = 0
      constraints.weighty = 0
      constraints.fill = GridBagPanel.Fill.Horizontal

      constraints.gridy = line
      constraints.weightx = 0.3
      constraints.insets = new Insets(0, 0, 4, 0)
      layout(label(text)) = constraints

      constraints.gridx = 1
      constraints.weightx = 0.7
      constraints.insets = new Insets(0, 4, 4, 0)
      layout(component) = constraints

      line = line + 1
    }

    def addSpace() {
      val constraints = new Constraints
      constraints.gridx = 0
      constraints.weighty = 0
      constraints.gridwidth = 2
      constraints.gridy = line
      layout(new Label("<html><br /></html>")) = constraints
      line = line + 1
    }

    private[UserOptions] def fillToBottom() {
      val c = new Constraints
      c.fill = GridBagPanel.Fill.Vertical
      c.gridy = line
      c.gridx = 0
      c.weighty = 1
      layout(new Label("")) = c
    }

    private[UserOptions] def addTitle() {
      val constraints = new Constraints
      constraints.gridx = 0
      constraints.gridy = 0
      constraints.weighty = 0
      constraints.gridwidth = 2
      constraints.insets = new Insets(8, 8, 8, 8)
      constraints.fill = GridBagPanel.Fill.Horizontal

      val label = new Label("<html>"+htmlDescrition+"</html>")
      label.maximumSize = new Dimension(30, 800);
      layout(label) = constraints
    }
  }

  protected val defaultResult: Map[String, String]
  protected var result: Map[String, String] = Map()

  protected val htmlDescription: String
  protected def fillPanel(panel: CustomizedGridBagPanel)

  def getUserInput: Map[String, String] = result
  def getPanel(): Panel = {
    result = defaultResult
    val panel = new CustomizedGridBagPanel(htmlDescription)
    panel.addTitle
    fillPanel(panel)
    panel.fillToBottom
    panel
  }

  protected def label(text: String) = {
    val label = new Label(text+":")
    label.horizontalAlignment = Alignment.Right
    label
  }

  protected def text(name: String) = {
    val field = new TextField
    field.reactions += {
      case ValueChanged(_) => result += (name -> field.text)
    }
    field.text = result(name)
    field
  }

  protected def password(name: String) = {
    val field = new PasswordField(result(name))
    field.reactions += {
      case ValueChanged(_) => result += (name -> field.password.mkString(""))
    }
    field
  }

  protected def checkbox(name: String, text: String) = {
    val field = new CheckBox(text)
    field.text = text
    field.selected = result(name).toLowerCase == "true"
    field.reactions += {
      case ButtonClicked(`field`) => result += (name -> field.selected.toString.toLowerCase)
    }
    field
  }

  protected def combobox(name: String, texts: Seq[String]): ComboBox[String] = {
    val box = new ComboBox(texts)
    box.selection.item = result(name)
    box.selection.reactions += {
      case SelectionChanged(`box`) => result += (name -> box.selection.item)
    }
    box
  }

  protected def combobox(name: String, texts: Set[String]): ComboBox[String] =
    combobox(name, texts.toSeq)
}