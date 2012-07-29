package de.jowisoftware.mining.analyzer.workflow

import de.jowisoftware.mining.UserOptions

class WorkflowUserOptions extends UserOptions {
  var result: Map[String, String] = Map(
    "dot" -> """c:\Program Files (x86)\Graphviz2.26.3\bin\dot.exe""",
    "visualization" -> "Matrix")

  def getHtmlDescription() = """<b>Structure Analyzer</b>"""

  def fillPanel(panel: CustomizedGridBagPanel) = {
    panel.add("Visualization", combobox("visualization", Seq("Graph", "Matrix")))
    panel.add("Dot executable", text("dot"))
  }
}