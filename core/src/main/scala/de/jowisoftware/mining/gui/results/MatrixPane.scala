package de.jowisoftware.mining.gui.results

import scala.swing.Table
import javax.swing.table.DefaultTableModel
import scala.swing.ScrollPane
import java.awt.Dimension
import javax.swing.JViewport
import de.jowisoftware.mining.analyzer.MatrixResult

class MatrixPane(matrixResult: MatrixResult) extends ScrollPane {
  private val normalizedRows = matrixResult.normalizedRows
  private val maxCols = normalizedRows.map(_.max)
  private val minCols = normalizedRows.map(_.min)

  private val values = normalizedRows.zipWithIndex.map {
    case (row, i) => row map { cell =>
      val text = (if (cell.isNaN) 0f else cell * 100).formatted("%.2f %%")

      if (cell == minCols(i) && matrixResult.highlight) "<html><font color=\"blue\">"+text+"</font></html>"
      else if (cell == maxCols(i) && matrixResult.highlight) "<html><font color=\"red\">"+text+"</font></html>"
      else text
    }
  }

  private val tableModel = new DefaultTableModel(0, values(0).size) {
    override def isCellEditable(x: Int, y: Int) = false
    override def getColumnName(column: Int) = headers(column)

    private val headers = matrixResult.columnTitles

    for ((rowData, row) <- values.zipWithIndex) {
      this.addRow(rowData.toArray[Object])
    }
  }

  private val rowHeaderColumnModel = new DefaultTableModel(0, 1) {
    override def isCellEditable(x: Int, y: Int) = false

    private val rowHeaders = matrixResult.rowTitles
    for (i <- 0 until tableModel.getRowCount()) {
      this.addRow(Array[Object](rowHeaders(i)))
    }
  }

  private val table = new Table
  table.model = tableModel
  private val rowHeaderTable = new Table
  rowHeaderTable.model = rowHeaderColumnModel

  table.peer.setSelectionModel(rowHeaderTable.peer.getSelectionModel())

  rowHeaderTable.maximumSize = new Dimension(80, 10000)

  rowHeaderTable.background = table.peer.getTableHeader().getBackground()
  rowHeaderTable.foreground = table.peer.getTableHeader().getForeground()

  private val viewport = new JViewport()
  viewport.setView(rowHeaderTable.peer)
  viewport.setPreferredSize(rowHeaderTable.maximumSize)

  table.autoResizeMode = Table.AutoResizeMode.Off
  rowHeaderTable.autoResizeMode = Table.AutoResizeMode.Off

  contents = table
  peer.setRowHeader(viewport)
}