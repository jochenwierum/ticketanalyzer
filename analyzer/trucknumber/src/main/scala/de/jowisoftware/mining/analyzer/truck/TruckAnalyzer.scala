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

object TruckAnalyzer {
  /**
    * Maps all querys into a generator function which generate the query.
    */
  val queryMap: Map[String, (DBWithTransaction[RootNode], Int) => String] =
    Map("Ciritcal keywords" -> {
      case (db, limit) =>
        val node = db.rootNode.keywordCollection.id
        """START n=node(%d) // keyword collection
        MATCH n --> keyword --> ticket <-[:owns]- person
        RETURN keyword.name AS keyword,
          (count(distinct ticket.id) / count(distinct person.name)) AS ratio,
          count(distinct ticket.id) AS ticketCount,
          count(distinct person.name) AS personCount,
          collect(distinct person.name) AS persons,
          collect(distinct ticket.id) AS tickets
        ORDER BY (ratio) DESC
        LIMIT %d;""" format (node, limit)
    },
      "Ciritcal files" -> {
        case (db, limit) =>
          val nodes = db.rootNode.commitRepositoryCollection.neighbors(
            Direction.OUTGOING, Seq(Contains.relationType))
            .map(_.asInstanceOf[CommitRepository].files.id)
            .mkString(", ")
          """START n=node(%s) // commit collection -> file collection
        MATCH n-->file<-[:changed_file]-commit-[:owns]->person
        RETURN
          file.name as filename,
          count(distinct commit.id) / count(distinct person.name) as factor,
          count(commit.id) as commitCount,
          count(distinct person.name) as personCount,
          collect(distinct person.name) as persons
        ORDER BY factor DESC
        LIMIT %d;""" format (nodes, limit)
      })
}

class TruckAnalyzer extends Analyzer {
  def userOptions() = new TruckAnalyzerOptions

  def analyze(db: Database[RootNode], options: Map[String, String],
    parent: Frame, waitDialog: ProgressDialog) {

    val queryString = db.inTransaction { transaction =>
      val queryString = TruckAnalyzer.queryMap(options("algorithm"))(transaction, 50)
      transaction.success
      queryString
    }

    val engine = new ExecutionEngine(db.service)
    val result = engine.execute(queryString)

    Swing.onEDTWait {
      waitDialog.close
      new ResultWindow(parent, result).visible = true
    }
  }
}