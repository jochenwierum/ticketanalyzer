package de.jowisoftware.mining.analyzer.workflow

import scala.swing.Dialog
import scala.swing.Table
import javax.swing.table.DefaultTableModel
import java.awt.Dimension
import java.awt.BorderLayout
import scala.swing.BorderPanel
import scala.swing.Label

class MatrixDialog(matrix: TextMatrix) extends Dialog {
  title = "Propability Matrix"

  private val description = new Label("""<html><p>This window shows the possibility that
      | a ticket changes its state (left) to another state (top). <br />The
      | value &quot;(final)&quot; means, that the ticket will stay in the
      | current state.</p></html>""".stripMargin)

  private val content = new BorderPanel
  content.layout(createTable) = BorderPanel.Position.Center
  content.layout(description) = BorderPanel.Position.North

  contents = content

  modal = true
  resizable = false
  pack()
  centerOnScreen()

  private def createTable = {
    val headers = "" +: matrix.columnTitles.map { "<html><b>"+_+"</b></html>" }
    val rowHeaders = matrix.rowTitles.map { "<html><b>"+_+"</b></html>" }

    val values = matrix.normalizedRows.map {
      _ map { cell =>
        if (cell.isNaN) "0.0"
        else cell.formatted("%.2f")
      }
    }

    val tableModel = new DefaultTableModel(0, values(0).size + 1) {
      override def isCellEditable(x: Int, y: Int) = false

      this.addRow(headers.toArray[Object])

      println(rowHeaders.deep.toString)
      println(values.deep.toString)
      for ((rowData, row) <- values.zipWithIndex) {
        this.addRow((rowHeaders(row) +: rowData).toArray[Object])
      }
    }

    new Table() {
      model = tableModel
    }
  }
}