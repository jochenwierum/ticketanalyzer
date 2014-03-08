package de.jowisoftware.demo.historyGenerator

import scala.collection.SortedMap

import org.neo4j.cypher.ExecutionResult

import de.jowisoftware.mining.analyzer.{ AnalyzerResult, TextResult }
import de.jowisoftware.mining.gui.ProgressMonitor
import de.jowisoftware.mining.model.nodes.Version
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }

object HistoryGeneratorAnalyzer {
  private val versionMatcher = """(\d+\.\d+(?:\.\d+)*)""".r
}

class HistoryGeneratorAnalyzer(t: DBWithTransaction, options: Map[String, String],
    waitDialog: ProgressMonitor) {

  def run(): AnalyzerResult = {
    val executionResult = getResults(t)
    val tableMap = transformToTable(executionResult)
    val table = transformToString(tableMap)
    new TextResult(table, "History Generator")
  }

  private def getResults(transaction: DBWithTransaction): ExecutionResult = {
    val query = s"""
      MATCH ${Version.cypherForAll("version")}<-[:targets]-ticket
      WHERE NOT (ticket) <-[:updates]- () AND version.name <> ""
      RETURN version.name AS version, ticket.title AS title
      ORDER BY ticket.updateDate ASC"""

    transaction.cypher(query)
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
