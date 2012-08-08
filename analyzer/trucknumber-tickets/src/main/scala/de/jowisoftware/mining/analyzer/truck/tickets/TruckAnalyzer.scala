package de.jowisoftware.mining.analyzer.truck.tickets

import scala.swing._

import org.neo4j.graphdb.Direction

import de.jowisoftware.mining.analyzer.truck.tickets.filters._
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.mining.model.relationships.{ Links, Owns }
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }

class TruckAnalyzer(db: Database[RootNode], options: Map[String, String],
    parent: Frame, waitDialog: ProgressDialog) {

  def run() {
    require(options contains "limit")
    require(options("limit").matches("""\d+"""), "Limit is not numeric")

    val resultWindow = createCriticalKeywordsWindow

    Swing.onEDTWait {
      waitDialog.hide()
      resultWindow.visible = true
    }
  }

  private def createCriticalKeywordsWindow: Dialog =
    db.inTransaction { transaction =>
      val limit = options("limit").toInt
      val ignoredList = options("inactive").trim.split("""\s*,\s*""")
      val activePersons = getActivePersons(transaction, ignoredList).toSet
      val filters = createFilters(options)

      val groupedRows = findOccurrences(transaction, activePersons, filters.accept)
      val result = formatResult(groupedRows, limit)

      if (options("output") == "raw")
        new ResultWindow(parent, result.iterator, Seq("keyword", "ratio",
          "ticketCount", "personCount", "tickets", "persons"))
      else
        new KeywordResultWindow(parent, result.iterator, activePersons)
    }

  private def createFilters(options: Map[String, String]) = {
    def isSet(name: String) = options(name).toLowerCase() == "true"

    val filter = new AndCollectionFilter()
    if (isSet("filter-change-status")) filter add PersonChangedStatusFilter
    if (isSet("filter-reporter")) filter add NonReporterFilter
    if (isSet("filter-comment")) filter add WroteCommentFilter
    if (isSet("filter-commit")) filter add PersonHasComittedFilter
    if (isSet("filter-status")) filter add StatusFilter
    if (isSet("filter-comment-commit"))
      filter add new OrCollectionFilter(PersonHasComittedFilter, WroteCommentFilter)

    filter
  }

  private def getActivePersons(transaction: DBWithTransaction[RootNode], ignoredList: Array[String]): Seq[String] =
    transaction.rootNode.personCollection.children
      .map(_.name())
      .filterNot(name => name == "" || ignoredList.contains(name))
      .toSeq

  private def findOccurrences(transaction: DBWithTransaction[RootNode],
    activePersons: Set[String],
    accept: (Keyword, Ticket, Person) => Boolean): Seq[Map[String, Any]] = {

    waitDialog.status = "Counting keywords"
    waitDialog.max = transaction.rootNode.keywordCollection.children.size
    waitDialog.status = "Searching keyword occurrences"

    (transaction.rootNode.keywordCollection.children.map { keyword =>
      waitDialog.tick()

      val occurrences = findKeywordOccurrences(activePersons, accept, keyword)

      val tickets = occurrences.map(_("ticket")).toSet
      val persons = occurrences.map(_("person")).toSet
      val ratio = tickets.size * (1 - persons.size.floatValue() / activePersons.size)

      Map("keyword" -> keyword.name(), "ratio" -> ratio, "ticketCount" -> tickets.size,
        "personCount" -> persons.size, "tickets" -> tickets,
        "persons" -> persons)
    }).toList
  }

  private def formatResult(queryResult: Seq[Map[String, Any]], limit: Int): Seq[Map[String, Any]] =
    queryResult.sortBy(_("ratio").asInstanceOf[Float]).reverse take limit

  private def findKeywordOccurrences(activePersons: Set[String], accept: (Keyword, Ticket, Person) => Boolean,
    keyword: Keyword): List[Map[String, Any]] =
    (keyword.neighbors(Direction.OUTGOING, Seq(Links.relationType)).collect {
      case ticket: Ticket =>
        ticket.neighbors(Direction.INCOMING, Seq(Owns.relationType)).collect {
          case person: Person if ((activePersons contains person.name()) && accept(keyword, ticket, person)) =>
            Map("ticket" -> ticket.ticketId(), "person" -> person.name())
        }
    }).flatten.toList
}