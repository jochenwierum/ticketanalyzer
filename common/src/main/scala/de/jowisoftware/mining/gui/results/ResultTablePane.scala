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
          rowData(columnMap(key)) = anyToCellData(value)
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

  private def anyToCellData(obj: Any): CellData = obj match {
    case null => CellData("(null)", "(null)")
    case node: Node =>
      CellData(formatNode(node, identity),
        clearNamespaces(formatNode(node, shortenString(40))))
    case rel: Relationship =>
      CellData(formatRelationship(rel, identity),
        clearNamespaces(formatRelationship(rel, shortenString(40))))
    case _ => CellData(anyToString(obj, identity), anyToString(obj, shortenString(40)))
  }

  private def anyToString(obj: Any, formatter: String => String): String = {
    val unmasked = obj match {
      case n @ (_: Float | _: Double) =>
        n.formatted("%.4f")
      case a: Array[_] =>
        a.map(x => formatter(x.toString)).deep.toString
      case x => formatter(x.toString)
    }

    unmasked.replace("\\", "\\\\").replace("\n", "\\n")
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
  }

  private def clearNamespaces(text: String): String =
    text.replace("de.jowisoftware.mining.model.", "")

  private def formatNode(node: Node, formatter: String => String): String =
    "<b>Node["+node.getId+"]: "+getClassFromProperties(node)+
      "</b><br />\n"+formatProperties(node, formatter)

  private def formatRelationship(relationship: Relationship, formatter: String => String): String =
    "<b>Relationship["+relationship.getId+"]: "+getClassFromProperties(relationship)+
      "</b><br />\n"+formatProperties(relationship, formatter)

  private def getClassFromProperties(properties: PropertyContainer): String =
    properties.getProperty("_class", "(?)").toString

  private def formatProperties(properties: PropertyContainer, formatter: String => String) =
    formatPropertyLines(properties, formatter).mkString("<br />\n")

  private def formatPropertyLines(properties: PropertyContainer, formatter: String => String): Seq[String] =
    properties.getPropertyKeys.toSeq.sorted.withFilter(!_.startsWith("_")).map { property =>
      property+": "+anyToString(properties.getProperty(property), formatter)
    }

  private def shortenString(length: Int)(text: String): String =
    if (text.length > length) text.substring(0, length - 3)+"..." else text
}