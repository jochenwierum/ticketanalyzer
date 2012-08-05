package de.jowisoftware.mining.gui.results

import scala.collection.JavaConversions._
import scala.swing.ScrollPane
import org.neo4j.cypher.ExecutionResult
import javax.swing.table.AbstractTableModel
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.PropertyContainer
import scala.swing.Swing

class ResultTablePane(result: ExecutionResult) extends ScrollPane {
  def this() = this(null)

  private val table = new ResultTable()
  contents = table

  if (result != null)
    processResult(result)

  def processResult(result: ExecutionResult) {
    val columnNames = result.columns
    val columnMap = Map(columnNames.zipWithIndex: _*)

    val tableData: Array[Array[CellData]] = (result map { row =>
      val rowData: Array[CellData] = new Array[CellData](columnNames.size)
      row.foreach({
        case (key, value) =>
          rowData(columnMap(key)) = CellFormatter.anyToCellData(value)
      })

      rowData
    }).toArray

    Swing.onEDT {
      updateTable(tableData, columnNames.toSeq)
    }
  }

  private def updateTable(tableData: Array[Array[CellData]], columnNames: Seq[String]) {
    table.model = new AbstractTableModel() {
      override def getColumnName(col: Int) = columnNames(col)
      def getColumnCount() = columnNames.size
      def getRowCount = tableData.length
      def getValueAt(row: Int, column: Int) = tableData(row)(column)
    }
  }

}