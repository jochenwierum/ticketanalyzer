package de.jowisoftware.mining.analyzer.roles

import scala.collection.immutable.Map
import scala.swing.Frame
import org.neo4j.cypher.{ ExecutionEngine, ExecutionResult }
import de.jowisoftware.mining.analyzer.Analyzer
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.linker.StatusType
import scala.collection.mutable
import de.jowisoftware.mining.analyzer.data.TextMatrix

class RolesAnalyzer extends Analyzer {
  def userOptions() = new RolesOptions()

  def analyze(db: Database[RootNode], options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) = {
    val stateMap = buildStateMap(db)

    val matrix = new TextMatrix(
      StatusType.values.filter(_ != StatusType.ignore).map(StatusType.roleName).toSeq.sorted,
      stateMap.keys.toSeq.sorted)

    for {
      (person, sumMap) <- stateMap
      (role, count) <- sumMap
      if (role != StatusType.ignore)
    } {
      matrix.set(StatusType.roleName(role), person, count)
    }

    waitDialog.hide()
    new ResultDialog(matrix, parent).visible = true
  }

  private def findStateCountByName(db: Database[RootNode]): ExecutionResult = {
    val query = """
      START persons=node(%d)
      MATCH persons --> person -[:owns]-> ticket -[:has_status]-> status
      WHERE person.name <> ""
      RETURN person.name AS name, status.logicalType AS status, count(distinct ticket.id) AS count
      """ format (db.rootNode.personCollection.id)

    val engine = new ExecutionEngine(db.service)
    val result = engine.execute(query)
    result
  }

  private def findReporterCountByName(db: Database[RootNode]): ExecutionResult = {
    val query = """
        START persons=node(%d)
        MATCH persons --> person -[:created]-> ticket
        WHERE person.name <> ""
        RETURN person.name AS name, count(distinct ticket.id) AS count
        """ format (db.rootNode.personCollection.id)

    val engine = new ExecutionEngine(db.service)
    val result = engine.execute(query)
    result
  }

  private def buildStateMap(db: Database[RootNode]): Map[String, mutable.Map[StatusType.Value, Long]] = {
    var personStateCounter: Map[String, mutable.Map[StatusType.Value, Long]] = Map()

    def addCount(name: String, state: StatusType.Value, count: Long) {
      val stateMap = personStateCounter.getOrElse(name,
        { personStateCounter += name -> mutable.Map(); personStateCounter(name) })

      stateMap(state) = (stateMap.getOrElse(state, 0L) + count)
    }

    for (stateWithName <- findStateCountByName(db)) {
      val person = stateWithName("name").asInstanceOf[String]
      val state = StatusType(stateWithName("status").asInstanceOf[Int])
      val count = stateWithName("count").asInstanceOf[Long]
      addCount(person, state, count)
    }

    for (reporterWithCount <- findReporterCountByName(db)) {
      val person = reporterWithCount("name").asInstanceOf[String]
      val count = reporterWithCount("count").asInstanceOf[Long]

      addCount(person, StatusType.reported, count)
    }

    personStateCounter
  }
}