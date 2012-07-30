package de.jowisoftware.mining.analyzer.data

import scala.swing.Table
import javax.swing.table.DefaultTableModel

class TextMatrixSwingTable(matrix: TextMatrix) extends Table {
  private val headers = "" +: matrix.columnTitles.map { "<html><b>"+_+"</b></html>" }
  private val rowHeaders = matrix.rowTitles.map { "<html><b>"+_+"</b></html>" }

  private val values = matrix.normalizedRows.map {
    _ map { cell =>
      if (cell.isNaN) "0.0"
      else cell.formatted("%.2f")
    }
  }

  private val tableModel = new DefaultTableModel(0, values(0).size + 1) {
    override def isCellEditable(x: Int, y: Int) = false

    this.addRow(headers.toArray[Object])

    println(rowHeaders.deep.toString)
    println(values.deep.toString)
    for ((rowData, row) <- values.zipWithIndex) {
      this.addRow((rowHeaders(row) +: rowData).toArray[Object])
    }
  }

  model = tableModel
}