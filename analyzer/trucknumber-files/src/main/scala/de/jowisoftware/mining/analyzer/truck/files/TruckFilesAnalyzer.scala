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

  def analyze(db: Database[RootNode], options: Map[String, String],
    parent: Frame, waitDialog: ProgressDialog) {

    val resultWindow = createCiriticalFilesWindow(db, options, parent)

    Swing.onEDTWait {
      waitDialog.hide()
      resultWindow.visible = true
    }
  }

  private def createCiriticalFilesWindow(db: Database[RootNode],
    options: Map[String, String], parent: Frame): Dialog =
    db.inTransaction { transaction =>
      val nodes = db.rootNode.commitRepositoryCollection.neighbors(
        Direction.OUTGOING, Seq(Contains.relationType))
        .map(_.asInstanceOf[CommitRepository].files.id)
        .mkString(", ")

      val query = """START n=node(%s) // commit collection -> file collection
      MATCH n-->file<-[:changed_file]-commit<-[:owns]-person
      %s
      RETURN
        file.name as file,
        count(distinct commit.id) / count(distinct person.name) as ratio,
        count(commit.id) as commitCount,
        count(distinct person.name) as personCount,
        collect(distinct person.name) as persons
      ORDER BY ratio DESC
      LIMIT %d;""" format (nodes, ignoreToWhereClauses(options("inactive")),
        options("limit").toInt)

      val result = new ExecutionEngine(db.service).execute(query)

      if (options("output") == "raw")
        new ResultWindow(parent, result)
      else
        new KeywordResultWindow(parent, result, getActivePersons(transaction,
          options("inactive").split("""\s*,\s*""")).toSet, "File")
    }

  private def ignoreToWhereClauses(ignore: String): String =
    """WHERE person.name <> "" """+(if (ignore.trim.isEmpty) ""
    else
      ignore.split("""\s*,\s*""").map("person.name <> \""+_+"\"").mkString(" AND ", " AND ", ""))

  private def getActivePersons(transaction: DBWithTransaction[RootNode], ignoredList: Array[String]): Seq[String] = {
    val activePersons = transaction.rootNode.personCollection.children
      .map(_.name())
      .filterNot(name => name == "" || ignoredList.contains(name))
      .toSeq
    activePersons
  }
}