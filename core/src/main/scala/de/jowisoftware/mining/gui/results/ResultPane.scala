package de.jowisoftware.mining.gui.results

import scala.swing.ScrollPane
import java.io.OutputStream

object ResultPane {
  case class SaveDescription(pattern: String, text: String)
}

trait ResultPane { this: ScrollPane =>
  val saveDescription: ResultPane.SaveDescription
  def saveToStream(stream: OutputStream): Unit
}