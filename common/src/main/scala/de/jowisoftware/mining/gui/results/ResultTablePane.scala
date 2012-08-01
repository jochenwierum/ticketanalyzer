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
          rowData(columnMap(key)) = anyToString(value)
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

  private def anyToString(obj: Any): CellData =
    obj match {
      case null => CellData("(null)", "(null)")
      case node: Node =>
        CellData(formatNode(node, identity),
          clearNamespaces(formatNode(node, shortenString(40))))
      case rel: Relationship =>
        CellData(formatRelationship(rel, identity),
          clearNamespaces(formatRelationship(rel, shortenString(40))))
      case n @ (_: Float | _: Double) =>
        val formatted = n.formatted("%.4f")
        CellData(formatted, formatted)
      case x => CellData(x.toString, shortenString(40)(x.toString))
    }

  private def clearNamespaces(text: String) =
    text.replace("de.jowisoftware.mining.model.", "")

  private def formatNode(node: Node, formatter: String => String) =
    "<b>Node["+node.getId+"]: "+getClassFromProperties(node)+"</b>"+formatProperties(node, formatter)

  private def formatRelationship(relationship: Relationship, formatter: String => String) =
    "<b>Relationship["+relationship.getId+"]: "+getClassFromProperties(relationship)+
      "</b>"+formatProperties(relationship, formatter)

  private def formatProperties(properties: PropertyContainer, formatter: String => String) =
    formatPropertyLines(properties).map("<br>\n"+formatter(_)).mkString("")

  private def getClassFromProperties(properties: PropertyContainer) =
    properties.getProperty("_class", "(?)").toString

  private def formatPropertyLines(properties: PropertyContainer) =
    properties.getPropertyKeys.filter(!_.startsWith("_")).map { property =>
      property+": "+properties.getProperty(property).toString
        .replace("\\", "\\\\").replace("\n", "\\n")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
    }

  private def shortenString(length: Int)(text: String) =
    if (text.length > length) text.substring(0, length - 3)+"..." else text
}