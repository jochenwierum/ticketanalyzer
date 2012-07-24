package de.jowisoftware.mining.analyzer.structure

import de.jowisoftware.mining.UserOptions

class StructreUserOptions extends UserOptions {
  var result: Map[String, String] = Map(
    "dot" -> """c:\Program Files (x86)\Graphviz2.26.3\bin\dot.exe""")

  def getHtmlDescription() = """<b>Structure Analyzer</b>"""

  def fillPanel(panel: CustomizedGridBagPanel) = {}
}