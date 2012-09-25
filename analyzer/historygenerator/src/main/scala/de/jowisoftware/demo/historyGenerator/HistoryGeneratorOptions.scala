package de.jowisoftware.demo.historyGenerator

import de.jowisoftware.mining.UserOptions
import scala.swing.{ GridPanel, Panel }

class HistoryGeneratorOptions extends UserOptions("importer.HistoryGenerator") {
  protected val defaultResult: Map[String, String] = Map()

  protected val htmlDescription = """<p><b>History Generator</b><br>
    Sample plugin to generate release notes</p>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) {
  }
}
