package de.jowisoftware.mining.analyzer.workflow

import de.jowisoftware.mining.UserOptions

class WorkflowUserOptions extends UserOptions("analyzer.workflow") {
  protected val defaultResult: Map[String, String] = Map(
    "dot" -> "/usr/bin/dot",
    "visualization" -> "Matrix",
    "dpi" -> "72")

  protected val htmlDescription = """<b>Ticket Workflow Analyzer</b>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) = {
    panel.add("Visualization", combobox("visualization", Seq("Graph", "Matrix")))
    panel.add("Dot executable", text("dot"))
    panel.add("Scale (DPI)", text("dpi"))
  }
}