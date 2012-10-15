package de.jowisoftware.mining.analyzer.workflow

import scala.swing.Dialog
import scala.swing.Table
import javax.swing.table.DefaultTableModel
import java.awt.Dimension
import java.awt.BorderLayout
import scala.swing.BorderPanel
import scala.swing.Label
import de.jowisoftware.mining.analyzer.data.TextMatrix
import de.jowisoftware.mining.analyzer.data.TextMatrixSwingTable
import scala.swing.Frame

class MatrixDialog(matrix: TextMatrix, parent: Frame) extends Dialog(parent) {
  title = "Propability Matrix"

  private val description = new Label("""<html><p>This window shows the possibility that
      | a ticket changes its state (left) to another state (top). <br />The
      | value &quot;(final)&quot; means, that the ticket will stay in the
      | current state.</p></html>""".stripMargin)

  private val content = new BorderPanel
  content.layout(new TextMatrixSwingTable(matrix)) = BorderPanel.Position.Center
  content.layout(description) = BorderPanel.Position.North

  contents = content

  modal = true
  size = new Dimension(640, 480)
  resizable = true
  centerOnScreen()
}