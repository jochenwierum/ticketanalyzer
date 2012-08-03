package de.jowisoftware.mining.analyzer.workflow

import de.jowisoftware.mining.UserOptions

class WorkflowUserOptions extends UserOptions {
  protected val defaultResult: Map[String, String] = Map(
    "dot" -> """c:\Program Files (x86)\Graphviz2.26.3\bin\dot.exe""",
    "visualization" -> "Matrix",
    "dpi" -> "72")

  protected val htmlDescription = """<b>Ticket Workflow Analyzer</b>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) = {
    panel.add("Visualization", combobox("visualization", Seq("Graph", "Matrix")))
    panel.add("Dot executable", text("dot"))
    panel.add("Scale (DPI)", text("dpi"))
  }
}