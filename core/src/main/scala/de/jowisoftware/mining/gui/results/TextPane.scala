package de.jowisoftware.mining.gui.results

import de.jowisoftware.mining.analyzer.TextResult
import scala.swing._

class TextPane(result: TextResult) extends ScrollPane {
  private val textBox = new TextArea(result.content)
  textBox.editable = false

  contents = textBox
}