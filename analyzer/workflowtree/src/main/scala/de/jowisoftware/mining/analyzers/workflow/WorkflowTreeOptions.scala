package de.jowisoftware.mining.analyzers.workflow

import de.jowisoftware.mining.UserOptions
import scala.swing.{ GridPanel, Panel }

class WorkflowTreeOptions extends UserOptions("importer.WorkflowTree") {
  protected val defaultResult: Map[String, String] = Map(
    "dot" -> "/usr/bin/dot",
    "dpi" -> "72")

  protected val htmlDescription = """<p><b>Workflow (tree)</b></p>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Dot executable", text("dot"))
    panel.add("Scale (DPI)", text("dpi"))
  }
}
