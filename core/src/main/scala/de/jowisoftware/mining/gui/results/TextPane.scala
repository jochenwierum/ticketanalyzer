package de.jowisoftware.mining.gui.results

import de.jowisoftware.mining.analyzer.TextResult
import scala.swing._
import de.jowisoftware.mining.analyzer.AnalyzerResult
import java.io.OutputStream
import scala.io.Source
import java.io.OutputStreamWriter
import java.io.BufferedWriter
import java.io.PrintStream

class TextPane(result: TextResult) extends ScrollPane with ResultPane {
  private val textBox = new TextArea(result.content)
  textBox.editable = false

  contents = textBox

  val saveDescription = ResultPane.SaveDescription("txt", "Text file (*.txt)")
  def saveToStream(stream: OutputStream) = {
    val print = new PrintStream(stream, true, "UTF-8")
    result.content.split("""\n|<[Bb][Br]\s*/?>""").foreach(print.println)
    print.close()
  }
}