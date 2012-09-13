package de.jowisoftware.mining.analyzers.workflow

import de.jowisoftware.mining.UserOptions
import scala.swing.{ GridPanel, Panel }

class WorkflowTreeOptions extends UserOptions("importer.WorkflowTree") {
  protected val defaultResult: Map[String, String] = Map(
    "dot" -> "/usr/bin/dot",
    "dpi" -> "72",
    "highlight" -> "true",
    "ownerChange" -> "true",
    "nodeThreshold" -> "0.5",
    "edgeThreshold" -> "5.0")

  protected val htmlDescription = """<p><b>Workflow (tree)</b></p>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Dot executable", text("dot"))
    panel.add("Scale (DPI)", text("dpi"))

    panel.addSpace
    panel.add("highlight default path", checkbox("highlight", "Highlight the most used path"))
    panel.add("include owner change", checkbox("ownerChange", "Count owner change as new status"))
    panel.add("threshold for node in per cent", text("nodeThreshold"))
    panel.add("threshold for edge in per cent", text("edgeThreshold"))
  }
}
