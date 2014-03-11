package de.jowisoftware.mining.gui.results

import javax.swing.table.AbstractTableModel
import de.jowisoftware.mining.analyzer._
import scala.swing._
import de.jowisoftware.mining.gui.results.nodeTablePane._
import java.io.OutputStream
import au.com.bytecode.opencsv.CSVWriter
import java.io.OutputStreamWriter
import de.jowisoftware.util.HTMLUtil

class NodeTablePane(result: NodeResult) extends ScrollPane with ResultPane {
  private val table = new ResultTable()
  contents = table

  private val columnMap = Map(result.columnOrder.zipWithIndex: _*)

  private val tableData: Array[Array[CellData]] = (result.result map { row =>
    val rowData: Array[CellData] = new Array[CellData](result.columnOrder.size)
    row.foreach {
      case (key, value) =>
        rowData(columnMap(key)) = CellFormatter.anyToCellData(value)
    }

    rowData
  }).toArray

  updateTable(tableData, result.titles)

  private def updateTable(tableData: Array[Array[CellData]], columnNames: Seq[String]) {
    table.model = new AbstractTableModel() {
      override def getColumnName(col: Int) = columnNames(col)
      def getColumnCount() = columnNames.size
      def getRowCount = tableData.length
      def getValueAt(row: Int, column: Int) = tableData(row)(column)
    }
  }

  val saveDescription = ResultPane.SaveDescription("csv", "Table (*.csv)")
  def saveToStream(stream: OutputStream) = {
    val writer = new CSVWriter(new OutputStreamWriter(stream, "UTF-8"))

    writer.writeNext(result.titles.toArray)
    tableData.foreach { row =>
      val stringRow = row.map { cell =>
        if (cell.shortText != cell.longText)
          s"${cell.shortText} (${cell.longText})"
        else
          cell.shortText
      } map {
        HTMLUtil.stripHTML
      }
      writer.writeNext(stringRow)
    }

    writer.close()
  }
}