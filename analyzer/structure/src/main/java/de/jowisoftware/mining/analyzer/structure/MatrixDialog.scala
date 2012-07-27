package de.jowisoftware.mining.analyzer.structure

import scala.swing.Dialog
import scala.swing.Table
import javax.swing.table.DefaultTableModel
import java.awt.Dimension

class MatrixDialog(matrix: TextMatrix) extends Dialog {
  title = "Propability Matrix"

  contents = createTable

  modal = true
  resizable = false
  pack()
  centerOnScreen()

  def createTable = {
    val headers = "" +: matrix.columns.map{"<html><b>"+_+"</b></html>"}
    val values = matrix.normalizedValues.map {
      _ map { cell =>
        if (cell.isNaN) "0.0"
        else cell.formatted("%.2f")
      }
    }

    val tableModel = new DefaultTableModel(0, values.size + 1) {
      override def isCellEditable(x: Int, y: Int) = false

      this.addRow(headers.toArray[Object])

      for ((rowData, row) <- values.zipWithIndex) {
        this.addRow((headers(row + 1) +: rowData).toArray[Object])
      }
    }

    new Table() {
      model = tableModel
    }
  }
}