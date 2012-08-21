package de.jowisoftware.mining.analyzer.reviews

import scala.collection.mutable
import scala.swing.Frame
import org.neo4j.cypher.ExecutionEngine
import de.jowisoftware.mining.analyzer.Analyzer
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.linker.StatusType
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database
import org.neo4j.cypher.ExecutionResult
import scala.swing.Swing
import de.jowisoftware.util.HTMLUtil
import de.jowisoftware.mining.analyzer.data.TextMatrix

class ReviewAnalyzer extends Analyzer {
  def userOptions = new ReviewOptions

  def analyze(db: Database[RootNode], options: Map[String, String], parent: Frame,
    waitDialog: ProgressDialog) {
    val result = collectData(db)
    val ignored = options("nonDevs").trim.split("""\s*,\s*""")

    val names = db.rootNode.personCollection.children.map(_.name()).toSet -- ignored
    val sortedNames = names.toSeq.sorted
    val matrix = new TextMatrix(sortedNames, sortedNames)

    for (row <- result) {
      val from = row("from").asInstanceOf[String]
      val to = row("to").asInstanceOf[String]
      val count = row("count").asInstanceOf[Long]

      if ((names contains from) && (names contains to))
        matrix.set(to, from, count)
    }

    Swing.onEDT {
      waitDialog.hide()
      new ResultDialog(matrix, parent).visible = true
    }
  }

  private def collectData(db: Database[RootNode]): ExecutionResult = {
    val query = """
        START repositories=node(%d)
        MATCH
          repositories --> () --> ticket1 -[:has_status]-> state1,
          ticket1 <-[:updates]- ticket2 -[:has_status]-> state2,
          ticket1 <-[:owns]- person1,
          ticket2 <-[:owns]- person2
        WHERE state1.logicalType = %d AND state2.logicalType = %d
        RETURN person1.name AS from, person2.name AS to, count(distinct ticket1.id) as count
        """ format (db.rootNode.ticketRepositoryCollection.id,
      StatusType.assigned.id, StatusType.inReview.id)

    val engine = new ExecutionEngine(db.service)
    engine.execute(query)
  }
}