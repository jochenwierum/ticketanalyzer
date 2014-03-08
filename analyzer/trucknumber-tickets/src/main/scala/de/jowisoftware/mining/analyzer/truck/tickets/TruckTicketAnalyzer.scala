package de.jowisoftware.mining.analyzer.truck.tickets

import scala.swing._
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb.{ Direction, Node => NeoNode }
import de.jowisoftware.mining.analyzer.truck.tickets.filters._
import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.mining.model.relationships.{ Links, Owns }
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.mining.gui.ProgressMonitor
import de.jowisoftware.mining.analyzer.AnalyzerResult
import de.jowisoftware.mining.analyzer.NodeResult
import scala.collection.SortedSet

object TruckTicketsAnalyzer {
  private def mkLinearFunc(factor: Double)(value: Int, max: Int): Double =
    (1 - (value / (max * factor)) max 0)

  type WeightFunc = (Int, Int) => Double
  val weightAlgorithms = Map[String, WeightFunc](
    "linear" -> mkLinearFunc(1),
    "linear, 90 %" -> mkLinearFunc(.90),
    "linear, 80 %" -> mkLinearFunc(.80),
    "linear, 75 %" -> mkLinearFunc(.75),
    "linear, 50 %" -> mkLinearFunc(.50),
    "log" -> ((value: Int, max: Int) =>
      if (value == 0) 1
      else -math.log(value.doubleValue() / max) / math.log(max)))
}

class TruckTicketsAnalyzer(transaction: DBWithTransaction, options: Map[String, String],
    waitDialog: ProgressMonitor) {

  def run(): AnalyzerResult = {
    require(options contains "limit")
    require(options("limit").matches("""\d+"""), "Limit is not numeric")
    require(options contains "algorithm")
    require(options contains "members")
    require(options contains "members-action")

    require(options contains "min-commits")
    require(options contains "min-comments")
    require(options contains "min-commits2")
    require(options contains "min-comments2")
    require(options contains "min-state-changes")
    require(options("min-commits").matches("""\d+"""))
    require(options("min-comments").matches("""\d+"""))
    require(options("min-commits2").matches("""\d+"""))
    require(options("min-comments2").matches("""\d+"""))
    require(options("min-state-changes").matches("""\d+"""))

    createCriticalKeywordsWindow
  }

  private def createCriticalKeywordsWindow: AnalyzerResult = {
    val limit = options("limit").toInt
    val activePersons = getActivePersons(transaction)
    val filters = createFilters(options)

    val groupedRows = findOccurrences(activePersons, filters.accept, transaction)
    val result = formatResult(groupedRows, limit)

    if (options("output") == "raw") {
      val titles = Array("Keyword", "Ratio", "Ticket Count", "Person Count", "Tickets", "Persons")
      val fields = Array("keyword", "ratio", "ticketCount", "personCount", "tickets", "persons")
      new NodeResult(result.iterator, fields, titles, "Truck Number by tickets: raw result")
    } else {
      val titles = Array("Keyword", "Ratio", "Persons with knowledge", "Persons without knowledge")
      val fields = Array("keyword", "ratio", "personsWithKnowledge", "missingPersons")
      new NodeResult(transformToInterpreted(result.iterator, activePersons),
        fields, titles, "Truck Number by tickets")
    }
  }

  def transformToInterpreted(result: Iterator[Map[String, Any]], activePersons: Set[String]) = {
    val sortedActive: Set[String] = SortedSet.empty[String] ++ activePersons

    for (row <- result) yield {
      val persons = row("persons").asInstanceOf[Set[String]].toSet
      val missingPersons = sortedActive -- persons
      Map("keyword" -> row("keyword").asInstanceOf[String],
        "ratio" -> row("ratio").asInstanceOf[Double].toString,
        "personsWithKnowledge" -> persons.toSeq.sorted.mkString(", "),
        "missingPersons" -> missingPersons.toSeq.mkString(", "))
    }
  }

  private def createFilters(options: Map[String, String]) = {
    def isSet(name: String) = options(name).toLowerCase() == "true"

    val filter = new AndCollectionFilter()
    if (isSet("filter-change-status")) {
      filter add new PersonChangedStatusFilter(options("min-state-changes").toInt)
    }
    if (isSet("filter-reporter")) filter add NonReporterFilter
    if (isSet("filter-comment")) {
      filter add new WroteCommentFilter(options("min-comments").toInt)
    }
    if (isSet("filter-commit")) {
      filter add new PersonHasComittedFilter(options("min-commits").toInt)
    }
    if (isSet("filter-status")) filter add StatusFilter
    if (isSet("filter-comment-commit")) {
      filter add new OrCollectionFilter(
        new PersonHasComittedFilter((options("min-commits2").toInt)),
        new WroteCommentFilter((options("min-comments2").toInt)))
    }

    filter
  }

  private def getActivePersons(transaction: DBWithTransaction): Set[String] = {
    val members = options("members").trim.split("""\s*,\s*""")
    val namesView = transaction.cypher(s"MATCH ${Person.cypherForAll("node")} RETURN node.name AS name")
      .map(_.getOrElse("name", "").asInstanceOf[String])
      .filterNot(_ == "")

    val filteredView = if (options("members-action") == "included")
      namesView.filter(members.contains(_))
    else
      namesView.filterNot(members.contains(_))

    filteredView.toSet
  }

  private def findOccurrences(
    activePersons: Set[String],
    accept: (Keyword, Ticket, Person) => Boolean,
    db: DBWithTransaction): Seq[Map[String, Any]] = {

    waitDialog.status = "Counting keywords"
    waitDialog.max = db.cypher(s"MATCH ${Keyword.cypherForAll("node")} RETURN count(*) AS count").next()("count").asInstanceOf[Long]
    waitDialog.status = "Searching keyword occurrences"

    (db.cypher(s"MATCH ${Keyword.cypherForAll("node")} RETURN node").map { result =>
      val keyword = Node.wrapNeoNode(result("node").asInstanceOf[NeoNode], db, Keyword)
      waitDialog.tick()

      val occurrences = findKeywordOccurrences(activePersons, accept, keyword)

      val tickets = occurrences.map(_("ticket")).toSet
      val persons = occurrences.map(_("person")).toSet
      val ratio = tickets.size * TruckTicketsAnalyzer.weightAlgorithms(
        options("algorithm"))(persons.size, activePersons.size)

      Map("keyword" -> keyword.name(), "ratio" -> ratio, "ticketCount" -> tickets.size,
        "personCount" -> persons.size, "tickets" -> tickets,
        "persons" -> persons)
    }).toList
  }

  private def formatResult(queryResult: Seq[Map[String, Any]], limit: Int): Seq[Map[String, Any]] =
    queryResult.sortBy(_("ratio").asInstanceOf[Double]).reverse take limit

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