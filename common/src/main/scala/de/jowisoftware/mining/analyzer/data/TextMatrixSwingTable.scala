package de.jowisoftware.mining.analyzer.data

import scala.swing.Table
import javax.swing.table.DefaultTableModel

class TextMatrixSwingTable(matrix: TextMatrix, highlight: Boolean = false) extends Table {
  private val normalizedRows = matrix.normalizedRows
  private val maxCols = normalizedRows.map(_.max)
  private val minCols = normalizedRows.map(_.min)

  private val values = normalizedRows.zipWithIndex.map {
    case (row, i) => row map { cell =>
      val text = (if (cell.isNaN) 0f else cell * 100).formatted("%.2f %%")

      if (cell == minCols(i) && highlight) "<html><font color=\"blue\">"+text+"</font></html>"
      else if (cell == maxCols(i) && highlight) "<html><font color=\"red\">"+text+"</font></html>"
      else text
    }
  }

  private val tableModel = new DefaultTableModel(0, values(0).size + 1) {
    override def isCellEditable(x: Int, y: Int) = false

    private val headers = "" +: matrix.columnTitles.map { "<html><b>"+_+"</b></html>" }
    this.addRow(headers.toArray[Object])

    private val rowHeaders = matrix.rowTitles.map { "<html><b>"+_+"</b></html>" }
    for ((rowData, row) <- values.zipWithIndex) {
      this.addRow((rowHeaders(row) +: rowData).toArray[Object])
    }
  }

  model = tableModel
}