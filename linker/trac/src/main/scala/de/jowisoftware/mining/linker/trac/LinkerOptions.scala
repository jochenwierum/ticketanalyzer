package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.UserOptions
import scala.swing.GridPanel
import scala.swing.Panel

class LinkerOptions extends UserOptions {
  protected var result: Map[String, String] = Map()

  val getHtmlDescription = """<b>Trac Style Linker</b>"""

  def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Cache Tagnames", checkbox("cachetags", "enable caching"))
  }
}