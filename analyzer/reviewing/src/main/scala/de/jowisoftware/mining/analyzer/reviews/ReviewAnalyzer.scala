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

object ReviewAnalyzer {
  private val personQuery = "START p = node:person('*:*') RETURN p.name"
  private val query = """
        START repositoriy = node:repository('*:*')
        MATCH
          repository --> ticket1 -[:has_status]-> state1,
          ticket1 <-[:updates]- ticket2 -[:has_status]-> state2,
          ticket1 <-[:owns]- person1,
          ticket2 <-[:owns]- person2
        WHERE state1.logicalType = {type1} AND state2.logicalType = {type2}
        RETURN person1.name AS from, person2.name AS to, count(distinct ticket1.id) as count
        """

}

class ReviewAnalyzer extends Analyzer {
  def userOptions = new ReviewOptions

  def analyze(db: Database, options: Map[String, String], parent: Frame,
    waitDialog: ProgressDialog) {
    val executionEngine = new ExecutionEngine(db.service)

    val result = collectData(executionEngine)
    val nonDevs = options("nonDevs").trim.split("""\s*,\s*""")
    val ignored = options("ignore").trim.split("""\s*,\s*""")
    val other = "(other)"

    val names = findPersons(executionEngine) -- nonDevs -- ignored
    val sortedNames = names.toSeq.sorted
    val matrix = new TextMatrix(sortedNames :+ other, sortedNames :+ other)

    for (row <- result) {
      val from = row("from").asInstanceOf[String]
      val to = row("to").asInstanceOf[String]
      val count = row("count").asInstanceOf[Long]

      if (!(ignored contains from) && !(ignored contains to)) {
        val realFrom = if (names contains from) from else other
        val realTo = if (names contains to) to else other

        matrix.add(realTo, realFrom, count)
      }
    }

    Swing.onEDT {
      waitDialog.hide()
      new ResultDialog(matrix, parent).visible = true
    }
  }

  private def findPersons(executionEngine: ExecutionEngine): Set[String] = {
    executionEngine.execute(ReviewAnalyzer.personQuery).map(_.getOrElse("name", "").asInstanceOf[String]).toSet
  }

  private def collectData(engine: ExecutionEngine): ExecutionResult = {
    engine.execute(ReviewAnalyzer.query, Map(
      "type1" -> StatusType.assigned.id,
      "type2" -> StatusType.inReview.id))
  }
}