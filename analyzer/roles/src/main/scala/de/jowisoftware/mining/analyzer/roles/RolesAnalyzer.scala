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
import de.jowisoftware.neo4j.DBWithTransaction
import org.neo4j.shell.kernel.apps.Dbinfo
import de.jowisoftware.mining.model.nodes.Person

class RolesAnalyzer extends Analyzer {
  def userOptions() = new RolesOptions()

  def analyze(db: Database, options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) = {
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

  private def findStateCountByName(transaction: DBWithTransaction): ExecutionResult = {
    val query = s"""
      MATCH ${Person.cypherForAll("person")} -[:owns]-> ticket -[:has_status]-> status
      WHERE person.name <> ""
      RETURN person.name AS name, status.logicalType AS status, count(distinct ticket.id) AS count
      """

    transaction.cypher(query)
  }

  private def findReporterCountByName(transaction: DBWithTransaction): ExecutionResult = {
    val query = s"""
      MATCH ${Person.cypherForAll("person")} -[:created]-> ticket
      WHERE person.name <> ""
      RETURN person.name AS name, count(distinct ticket.id) AS count
      """

    transaction.cypher(query)
  }

  private def buildStateMap(db: Database): Map[String, mutable.Map[StatusType.Value, Long]] = db.inTransaction { transaction =>
    var personStateCounter: Map[String, mutable.Map[StatusType.Value, Long]] = Map()

    def addCount(name: String, state: StatusType.Value, count: Long) {
      val stateMap = personStateCounter.getOrElse(name,
        { personStateCounter += name -> mutable.Map(); personStateCounter(name) })

      stateMap(state) = (stateMap.getOrElse(state, 0L) + count)
    }

    for (stateWithName <- findStateCountByName(transaction)) {
      val person = stateWithName("name").asInstanceOf[String]
      val state = StatusType(stateWithName("status").asInstanceOf[Int])
      val count = stateWithName("count").asInstanceOf[Long]
      addCount(person, state, count)
    }

    for (reporterWithCount <- findReporterCountByName(transaction)) {
      val person = reporterWithCount("name").asInstanceOf[String]
      val count = reporterWithCount("count").asInstanceOf[Long]

      addCount(person, StatusType.reported, count)
    }

    personStateCounter
  }
}