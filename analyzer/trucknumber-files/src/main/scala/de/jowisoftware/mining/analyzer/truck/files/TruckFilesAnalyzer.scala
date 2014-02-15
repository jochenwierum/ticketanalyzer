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
      val engine = new ExecutionEngine(db.service)

      val query = """START n=node:commitRepository('*:*') // commit collection -> file collection
      MATCH n-[:contains_file]->file<-[:changed_file]-commit<-[:owns]-person
      %s
      RETURN
        file.name as file,
        count(distinct commit.id) / count(distinct person.name) as ratio,
        count(commit.id) as commitCount,
        count(distinct person.name) as personCount,
        collect(distinct person.name) as persons
      ORDER BY ratio DESC
      LIMIT {limit};""" format (ignoreToWhereClauses(options("inactive")))

      val result = engine.execute(query, Map("limit" -> options("limit").toInt))

      if (options("output") == "raw")
        new ResultWindow(parent, result)
      else
        new KeywordResultWindow(parent, result, getActivePersons(engine,
          options("inactive").split("""\s*,\s*""")).toSet, "File")
    }

  private def ignoreToWhereClauses(ignore: String): String =
    """WHERE person.name <> "" """+(if (ignore.trim.isEmpty) ""
    else
      ignore.split("""\s*,\s*""").map("person.name <> \""+_+"\"").mkString(" AND ", " AND ", ""))

  private def getActivePersons(engine: ExecutionEngine, ignoredList: Array[String]): Seq[String] =
    engine.execute("START n = node:person('*:*') RETURN n.name")
      .map(_.getOrElse("name", "").asInstanceOf[String])
      .filterNot(name => name == "" || ignoredList.contains(name))
      .toSeq
}