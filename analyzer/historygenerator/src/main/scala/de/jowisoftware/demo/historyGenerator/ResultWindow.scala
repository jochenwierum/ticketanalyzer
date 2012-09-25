package de.jowisoftware.demo.historyGenerator

import scala.swing.{ Dialog, Frame, ScrollPane, TextArea }

class ResultWindow(parent: Frame, text: String) extends Dialog(parent) {
  private val textBox = new TextArea(text)
  textBox.editable = false

  contents = new ScrollPane(textBox)

  title = "Changelog"
  resizable = true
  pack
  centerOnScreen
}