package de.jowisoftware.mining.analyzer.truck

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

class TruckAnalyzer extends Analyzer {
  private val algorithms: Map[String, (Database[RootNode], Map[String, String], Frame) => Dialog] = Map(
    "Critical keywords" -> createCriticalKeywordsWindow _,
    "Critical files" -> createCiriticalFilesWindow _)

  def userOptions() = new TruckAnalyzerOptions(algorithms.keySet)

  def analyze(db: Database[RootNode], options: Map[String, String],
    parent: Frame, waitDialog: ProgressDialog) {

    val resultWindow = algorithms(options("algorithm"))(db, options, parent)

    Swing.onEDTWait {
      waitDialog.hide()
      resultWindow.visible = true
    }
  }

  private def createCriticalKeywordsWindow(db: Database[RootNode],
    options: Map[String, String], parent: Frame): Dialog =

    db.inTransaction { transaction =>
      val ignoredList = options("inactive").split("""\s*,\s*""")
      val activePersons = getActivePersons(transaction, ignoredList)
      val personCount = activePersons.size
      val node = transaction.rootNode.keywordCollection.id

      val query = """START n=node(%d) // keyword collection
      MATCH n --> keyword --> ticket <-[:owns]- person
      %s
      RETURN keyword.name AS keyword,
        count(distinct ticket.id) * (1 - count(distinct person.name) / %d) AS ratio,
        count(distinct ticket.id) AS ticketCount,
        count(distinct person.name) AS personCount,
        collect(distinct ticket.id) AS tickets,
        collect(distinct person.name) AS persons
      ORDER BY (ratio) DESC
      LIMIT %d;""" format (node, ignoreToWhereClauses(options("inactive")),
        personCount, options("limit").toInt)

      val result = new ExecutionEngine(db.service).execute(query)

      if (options("output") == "raw")
        new ResultWindow(parent, result)
      else
        new KeywordResultWindow(parent, result, activePersons.toSet, "Keyword")
    }

  private def createCiriticalFilesWindow(db: Database[RootNode],
    options: Map[String, String], parent: Frame): Dialog =
    db.inTransaction { transaction =>
      val nodes = db.rootNode.commitRepositoryCollection.neighbors(
        Direction.OUTGOING, Seq(Contains.relationType))
        .map(_.asInstanceOf[CommitRepository].files.id)
        .mkString(", ")

      val query = """START n=node(%s) // commit collection -> file collection
      MATCH n-->file<-[:changed_file]-commit-[:owns]->person
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