package de.jowisoftware.mining.shell

import scala.collection.JavaConversions._
import scala.swing.{Table, ScrollPane}
import org.neo4j.cypher.ExecutionResult
import javax.swing.table.TableModel
import javax.swing.table.AbstractTableModel
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.PropertyContainer



class ResultTable extends ScrollPane {
  private val table = new Table()
  contents = table
  
  def processResult(result: ExecutionResult) {
    val columnNames = result.columns
    val columnMap = Map(columnNames.zipWithIndex: _* )
    
    val tableData: Array[Array[String]] = (result map { row =>
      val rowData: Array[String] = Array.fill(columnNames.size)("")
      row.foreach({case (key, value) => 
        rowData(columnMap(key)) = anyToString(value)
      })
      
      rowData
    }).toArray
    
    updateTable(tableData, columnNames.toSeq)
  }
  
  private def updateTable(tableData: Array[Array[String]], columnNames: Seq[String]) {
    table.model = new AbstractTableModel() {
      override def getColumnName(col: Int) = columnNames(col)
      def getColumnCount() = columnNames.size
      def getRowCount = tableData.length
      def getValueAt(row: Int, column: Int) = tableData(row)(column)
    }
  }
  
  private def anyToString(obj: Any): String = {
    obj match {
    case node: Node => formatNode(node)
    case rel: Relationship => formatRelationship(rel)
    case x => x.toString
    }
  }
  
  private def formatNode(node: Node) =
    "Node[" + node.getId + "]: " + node.getProperty(".class", "(?)") + formatProperties(node)
  
  private def formatRelationship(relationship: Relationship) =
    "Relationship[" + relationship.getId + "]: " + relationship.getProperty(".class", "(?)") + formatProperties(relationship)
  
  private def formatProperties(properties: PropertyContainer) = 
    properties.getPropertyKeys.filter(!_.startsWith(".")).map{ property =>
      val text = property +": "+ properties.getProperty(property).toString.replace("\\", "\\\\").replace("\n", "\\n")
      "\n" + (if (text.length > 30) text.substring(0, 27) + "..." else text)
    }.mkString("")
}