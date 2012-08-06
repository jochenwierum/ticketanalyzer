package de.jowisoftware.mining.analyzer.truck.files

import scala.swing.Dialog
import org.neo4j.cypher.ExecutionResult
import scala.swing.Frame
import scala.swing.Table
import javax.swing.table.DefaultTableModel
import scala.collection.SortedSet
import scala.swing.ScrollPane

class KeywordResultWindow(parent: Frame, result: Iterator[Map[String, Any]], activePersons: Set[String],
    firstTitle: String) extends Dialog(parent) {
  private val model = new DefaultTableModel(0, 4) {
    private val columnNames = Array(firstTitle, "Rating", "Persons with knowledge", "Persons without knowledge")
    private def add(cells: String*) = addRow(cells.toArray.asInstanceOf[Array[Object]])

    override def isCellEditable(row: Int, col: Int) = false
    override def getColumnName(col: Int) = columnNames(col)

    private val sortedActive: Set[String] = SortedSet.empty[String] ++ activePersons

    for (row <- result) {
      val persons = row("persons").asInstanceOf[List[String]].toSet
      val missingPersons = sortedActive -- persons
      add(row(firstTitle.toLowerCase).asInstanceOf[String], row("ratio").asInstanceOf[Double].toString,
        persons.toSeq.sorted.mkString(", "), missingPersons.toSeq.mkString(", "))
    }
  }

  private val table = new Table
  table.model = model
  contents = new ScrollPane(table)

  title = "Results"
  modal = true
  pack()
  centerOnScreen()
}