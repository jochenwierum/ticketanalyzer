package de.jowisoftware.mining.analyzers.workflow

import de.jowisoftware.mining.UserOptions
import scala.swing.{ GridPanel, Panel }

class WorkflowTreeOptions extends UserOptions("importer.WorkflowTree") {
  protected val defaultResult: Map[String, String] = Map()

  protected val htmlDescription = """<p><b>Workflow (tree)</b><br>
    Description of Workflow (tree)</p>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    // TODO: finish panel
  }
}
