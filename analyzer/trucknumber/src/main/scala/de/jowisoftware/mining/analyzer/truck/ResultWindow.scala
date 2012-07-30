package de.jowisoftware.mining.analyzer.truck

import scala.swing.{ Frame, Dialog }
import org.neo4j.cypher.ExecutionResult
import de.jowisoftware.mining.gui.results.{ ResultTablePane, ResultTable }
import java.awt.Dimension

class ResultWindow(parent: Frame, result: ExecutionResult) extends Dialog(parent) {
  contents = new ResultTablePane(result)

  title = "Result"
  minimumSize = new Dimension(640, 480)
  modal = true
  resizable = true

  centerOnScreen()
}