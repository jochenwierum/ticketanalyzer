package de.jowisoftware.demo.historyGenerator

import scala.swing.Frame
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.cypher.ExecutionResult
import scala.collection.SortedMap
import scala.swing.Swing

object HistoryGeneratorAnalyzer {
  private val versionMatcher = """(\d+\.\d+(?:\.\d+)*)""".r
}

class HistoryGeneratorAnalyzer(db: Database, options: Map[String, String],
    parent: Frame, waitDialog: ProgressDialog) {

  def run() {
    val executionResult = getResults
    val tableMap = transformToTable(executionResult)
    val table = transformToString(tableMap)

    Swing.onEDT {
      waitDialog.hide
      new ResultWindow(parent, table).visible = true
    }
  }

  private def getResults: ExecutionResult = {
    val query = """START version=node:version('*:*')
      MATCH
        version<-[:targets]-ticket<-[x?:updates]-newer
      WHERE newer IS NULL AND version.name <> ""
      RETURN version.name AS version, ticket.title AS title
      ORDER BY ticket.updateDate ASC"""

    val engine = new ExecutionEngine(db.service)
    engine.execute(query)
  }

  private def transformToTable(result: ExecutionResult) =
    (SortedMap.empty[String, List[String]](Ordering.fromLessThan(isLess)) /: result) {
      case (table, newRow) =>
        val version = newRow("version").asInstanceOf[String]
        val title = newRow("title").asInstanceOf[String]
        table + (version -> (title :: table.getOrElse(version, Nil)))
    }

  private def isLess(lhs: String, rhs: String): Boolean =
    (HistoryGeneratorAnalyzer.versionMatcher.findFirstIn(lhs),
      HistoryGeneratorAnalyzer.versionMatcher.findFirstIn(rhs)) match {
        case (Some(lhsMatch), Some(rhsMatch)) =>
          val v1 = lhsMatch.split("""\.""").map(_.toInt).toSeq
          val v2 = rhsMatch.split("""\.""").map(_.toInt).toSeq

          if (v1 == v2) false
          else v1 zipAll (v2, 0, 0) forall (pair => pair._1 >= pair._2)

        case (None, x) => false
        case (x, None) => true
      }

  private def transformToString(table: SortedMap[String, List[String]]) =
    table.map(row => "Version: "+row._1+"\n"+row._2.mkString("  * ", "\n  * ", "\n"))
      .mkString("\n")
}
