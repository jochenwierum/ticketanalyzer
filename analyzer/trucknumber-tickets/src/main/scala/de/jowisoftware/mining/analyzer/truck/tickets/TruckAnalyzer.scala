package de.jowisoftware.mining.analyzer.truck.tickets

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
import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.mining.model.relationships._
import de.jowisoftware.mining.analyzer.truck.tickets.filters.PersonHasComittedFilter
import de.jowisoftware.mining.analyzer.truck.tickets.filters._

class TruckAnalyzer extends Analyzer {
  def userOptions() = new TruckAnalyzerOptions()

  def analyze(db: Database[RootNode], options: Map[String, String],
    parent: Frame, waitDialog: ProgressDialog) {

    require(options contains "limit")
    require(options("limit").matches("""\d+"""), "Limit is not numeric")

    val resultWindow = createCriticalKeywordsWindow(db, options, parent)

    Swing.onEDTWait {
      waitDialog.hide()
      resultWindow.visible = true
    }
  }

  private def createCriticalKeywordsWindow(db: Database[RootNode],
    options: Map[String, String], parent: Frame): Dialog =
    db.inTransaction { transaction =>
      val limit = options("limit").toInt
      val ignoredList = options("inactive").split("""\s*,\s*""")
      val activePersons = getActivePersons(transaction, ignoredList).toSet
      val filters = new FilterAndCollection() //add PersonHasComittedFilter

      val rows = findRows(transaction, activePersons, filters.accept)
      val groupedRows = groupRows(rows, activePersons.size)
      val result = formatResult(groupedRows, limit)

      if (options("output") == "raw")
        new ResultWindow(parent, result.iterator, Seq("keyword", "ratio",
          "ticketCount", "personCount", "tickets", "persons"))
      else
        new KeywordResultWindow(parent, result.iterator, activePersons)
    }

  private def getActivePersons(transaction: DBWithTransaction[RootNode], ignoredList: Array[String]): Seq[String] =
    transaction.rootNode.personCollection.children
      .map(_.name())
      .filterNot(name => name == "" || ignoredList.contains(name))
      .toSeq

  private def findRows(transaction: DBWithTransaction[RootNode],
      activePersons: Set[String],
      accept: (Keyword, Ticket, Person) => Boolean): List[Map[String, Any]] = {

    var result: List[Map[String, Any]] = Nil
    transaction.rootNode.keywordCollection.children.flatMap { keyword =>
      keyword.neighbors(Direction.OUTGOING, Seq(Links.relationType)).collect { case ticket: Ticket =>
        ticket.neighbors(Direction.INCOMING, Seq(Owns.relationType)).collect {
          case person: Person if activePersons contains person.name() =>
            if (accept(keyword, ticket, person))
              result = Map("keyword" -> keyword.name(), "ticket" -> ticket.ticketId(), "person" -> person.name()) :: result
        }
      }
    }
    result
  }

  private def groupRows(queryResult: List[Map[String, Any]], personCount: Int): Seq[Map[String, Any]] = {
    val keywords = queryResult.groupBy(_("keyword").asInstanceOf[String])
    keywords.map {
      case (keyword, data) =>
        val tickets = data.map(_("ticket")).toSet
        val persons = data.map(_("person")).toSet
        val ratio = tickets.size * 1 - (persons.size.floatValue / personCount)

        Map("keyword" -> keyword, "ratio" -> ratio, "ticketCount" -> tickets.size,
          "personCount" -> persons.size, "tickets" -> tickets,
          "persons" -> persons)
    }.toList
  }

  private def formatResult(queryResult: Seq[Map[String, Any]], limit: Int): Seq[Map[String, Any]] =
    queryResult.sortBy(_("ratio").asInstanceOf[Float]).reverse take limit

}