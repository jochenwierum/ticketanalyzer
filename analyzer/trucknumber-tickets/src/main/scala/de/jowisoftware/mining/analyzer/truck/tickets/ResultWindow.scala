package de.jowisoftware.mining.analyzer.truck.tickets

import scala.swing.{ Frame, Dialog }
import org.neo4j.cypher.ExecutionResult
import de.jowisoftware.mining.gui.results.{ ResultTablePane, ResultTable }
import java.awt.Dimension

class ResultWindow(parent: Frame,
    result: Iterator[Map[String, Any]], columns: Seq[String]) extends Dialog(parent) {
  contents = new ResultTablePane(result, columns)

  title = "Result"
  minimumSize = new Dimension(640, 480)
  modal = true
  resizable = true

  centerOnScreen()
}