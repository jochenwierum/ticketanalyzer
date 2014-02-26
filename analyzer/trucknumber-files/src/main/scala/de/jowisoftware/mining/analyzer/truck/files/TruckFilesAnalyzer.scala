package de.jowisoftware.mining.analyzer.truck.files

import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.analyzer.Analyzer
import scala.swing.Frame
import de.jowisoftware.mining.model.nodes.RootNode
import org.neo4j.cypher.ExecutionEngine
import de.jowisoftware.mining.gui.ProgressDialog
import scala.swing.Swing
import de.jowisoftware.neo4j.DBWithTransaction
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.mining.model.nodes.CommitRepository
import scala.swing.Dialog
import de.jowisoftware.mining.model.nodes.Person

class TruckFilesAnalyzer extends Analyzer {
  def userOptions() = new TruckFilesAnalyzerOptions()

  def analyze(db: Database, options: Map[String, String],
    parent: Frame, waitDialog: ProgressDialog) {

    val resultWindow = createCiriticalFilesWindow(db, options, parent)

    Swing.onEDTWait {
      waitDialog.hide()
      resultWindow.visible = true
    }
  }

  private def createCiriticalFilesWindow(db: Database,
    options: Map[String, String], parent: Frame): Dialog =
    db.inTransaction { transaction =>
      val query = s"""
      MATCH ${CommitRepository.cypherForAll("n")}-[:contains_files]->file<-[:changed_file]-commit<-[:owns]-person
      WHERE NOT (person.name in ({ignored}))
      RETURN
        file.name as file,
        1.0 * count(distinct commit.id) / count(distinct person.name) as ratio,
        count(commit.id) as commitCount,
        count(distinct person.name) as personCount,
        collect(distinct person.name) as persons
      ORDER BY ratio DESC
      LIMIT {limit};"""

      val result = transaction.cypher(query,
        Map("limit" -> options("limit").toInt, "ignored" -> options("inactive").split("""\s*,\s*""").toArray))

      if (options("output") == "raw")
        new ResultWindow(parent, result)
      else
        new KeywordResultWindow(parent, result, getActivePersons(transaction,
          options("inactive").split("""\s*,\s*""")).toSet, "File")
    }

  private def getActivePersons(transaction: DBWithTransaction, ignoredList: Array[String]): Seq[String] =
    transaction.cypher(s"""MATCH ${Person.cypherForAll("n")} WHERE NOT n.name IN ({ignored}) RETURN n.name""",
      Map("ignored" -> ignoredList.toArray[String]))
      .map(_.getOrElse("name", "").asInstanceOf[String])
      .toSeq
}