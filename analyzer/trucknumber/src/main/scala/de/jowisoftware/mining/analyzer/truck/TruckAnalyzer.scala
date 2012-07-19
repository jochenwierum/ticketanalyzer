package de.jowisoftware.mining.analyzer.truck

import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.analyzer.Analyzer
import scala.swing.Frame
import de.jowisoftware.mining.model.nodes.RootNode
import org.neo4j.cypher.ExecutionEngine
import de.jowisoftware.mining.gui.ProgressDialog
import scala.swing.Swing

object TruckAnalyzer {
  private val query = """
    START n=node(%d)
    MATCH n --> keyword --> ticket <-[:owns]- person
    RETURN keyword.name AS keyword,
      (count(distinct ticket.id) / count(distinct person.name)) AS ratio,
      count(distinct ticket.id) AS ticketCount,
      count(distinct person.name) AS personCount,
      collect(distinct person.name) AS persons,
      collect(distinct ticket.id) AS tickets
    ORDER BY (ratio) DESC
    LIMIT %d;"""
}

class TruckAnalyzer extends Analyzer {
  def userOptions() = new TruckAnalyzerOptions

  def analyze(db: Database[RootNode], options: Map[String, String],
    parent: Frame, waitDialog: ProgressDialog) {

    val keywordRootId = db.inTransaction { transaction =>
      val id = transaction.rootNode.keywordCollection.id
      transaction.success
      id
    }
    val engine = new ExecutionEngine(db.service)
    val queryString = TruckAnalyzer.query format (keywordRootId, 20)
    val result = engine.execute(queryString)

    Swing.onEDTWait {
      waitDialog.close
      new ResultWindow(parent, result).visible = true
    }
  }
}