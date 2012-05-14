package de.jowisoftware.mining.importer.mantis

import java.util.Date

import scala.Option.option2Iterable
import scala.annotation.tailrec
import scala.collection.immutable.Stream.consWrapper
import scala.collection.immutable.Map
import scala.collection.SortedMap
import scala.xml.{NodeSeq, Elem}

import org.joda.time.format.DateTimeFormat

import MantisImporter.{fromSimpleDate, fromComplexDate, MantisConstants}
import de.jowisoftware.mining.importer.TicketData.TicketField._
import de.jowisoftware.mining.importer.{TicketData, TicketComment, Importer, ImportEvents}
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.util.XMLUtils._
import grizzled.slf4j.Logging

object MantisImporter {
  private val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  def fromComplexDate(value: String) = dateFormat.parseDateTime(value).toDate()

  private val simpleDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
  def fromSimpleDate(value: String) = simpleDateFormat.parseDateTime(value).toDate()

  object MantisConstants {
    val public = 10
  }
}

class MantisImporter extends Importer with Logging {
  import MantisImporter._

  def userOptions(): UserOptions = new MantisOptions()

  def importAll(config: Map[String, String], events: ImportEvents) {
    require(config contains "url")
    require(config contains "username")
    require(config contains "password")
    require(config contains "project")
    require(config contains "repositoryname")

    val client = new SoapClient(config("url"))

    processAllTickets(config, client, events)
  }

  private def processAllTickets(config: Map[String, String], client: SoapClient, events: ImportEvents) {
    info("Counting tickets...")
    events.countedTickets(countTickets(config, client))

    info("Importing tickets...")
    val scraper = new HTMLScraper(config("url"))
    scraper.login(config("username"), config("password"))

    val items = receiveTickets(config, client)
    processTicket(items(0), events, config("repositoryname"), scraper)
    //items foreach { t => processTicket(t, events, config("repositoryname"), scraper) }

    scraper.logout
    info("Importing finished.")
  }

  private def processTicket(item: Elem, events: ImportEvents, repository: String, scraper: HTMLScraper) {
    val id = (item \ "id").text.toInt
    val ticket = createTicket(item, repository, id)
    //val relationships =

    val html = scraper.readTicket(id)
    val historyTable = (html \\ "div" filter { tag => (tag \ "@id").text == "history_open" })(0)
    val historyRows = historyTable \\ "tr" drop 2

    val cp = new ChangeParser
    val tmp = historyRows.map(row => cp.parse(row, ticket))

    val changes: SortedMap[Date, (String, String, String)] =
      SortedMap.empty[Date, (String, String, String)] ++ historyRows.groupBy { row =>
        fromSimpleDate((row \ "td").head.text.trim)
      }.mapValues { row =>
        val cols = row \ "td"
        (cols(1).text.trim, cols(2).text.trim, cols(3).text.trim)
      }
    // events...

    processChanges(changes, ticket, repository, id).foreach { x =>
      //events...
    }
  }

  private def createTicket(item: Elem, repositoryName: String, ticketId: Int) = {
    def subnode(name: String) = item \ name \ "name" text
    def node(name: String) = item \ name text

    val reproducibility = subnode("reproducibility")
    val handler = subnode("handler")
    val eta = subnode("eta")

    val reporterName = subnode("reporter")

    import TicketData.TicketField._
    val ticket = TicketData(repositoryName, ticketId)
    ticket(summary) = node("summary") -> reporterName
    ticket(description) = (node("description")+"\n"+node("steps_to_reproduce")+"\n"+node("additional_information")) -> reporterName
    ticket(creationDate) = fromComplexDate(node("date_submitted")) -> reporterName
    ticket(updateDate) = fromComplexDate(node("last_updated")) -> reporterName
    ticket(version) = node("version") -> reporterName
    ticket(build) = node("build") -> reporterName
    ticket(status) = subnode("status") -> reporterName
    ticket(priority) = subnode("priority") -> reporterName
    ticket(reporter) = subnode("reporter") -> reporterName
    ticket(component) = node("category") -> reporterName
    ticket(resolution) = subnode("resolution") -> reporterName
    ticket(severity) = subnode("severity") -> reporterName
    ticket(fixedInVersion) = subnode("fixed_in_version") -> reporterName
    ticket(fixedInVersion) = subnode("fixed_in_version") -> reporterName
    ticket(comments) = getComments(item \ "notes") -> reporterName
    ticket(votes) = node("sponsorship_total").toInt -> reporterName
    ticket(environment) = (node("platform")+":"+node("os")+":"+node("osBuild")) -> reporterName

    //println(ticket)
    //println(item.formated)
    // reproducability

    ticket
  }

  private def getComments(item: NodeSeq) =
    (item \ "item").flatMap {
      case comment: Elem =>
        val public = (comment \ "view_state" \ "id" text).toInt == MantisConstants.public
        if (public) {
          Some(TicketComment(
            id = (comment \ "id" text) toInt,
            text = comment \ "text" text,
            author = comment \ "reporter" \ "name" text,
            submitted = fromComplexDate(comment \ "date_submitted" text),
            modified = fromComplexDate(comment \ "last_modified" text)))
        } else {
          None
        }
      case _ => None
    }

  private def processChanges(changes: SortedMap[Date, (String, String, String)], ticket: TicketData, repository: String, id: Int): Seq[TicketData] = {
    val reversedChanges = SortedMap.empty(changes.ordering.reverse) ++ changes.toList

    reversedChanges.foreach { x => }
    changes.foreach { x => }

    List()
  }

  private def countTickets(config: Map[String, String], client: SoapClient) = {
    def tickets(headers: NodeSeq) = headers \ "mc_project_get_issue_headersResponse" \ "return" \ "item" filter (_.isInstanceOf[Elem])
    val perPage = 25

    @tailrec def count(lastId: Int, lastCount: Int, page: Int): Int = {
      val items = tickets(getTicketHeaders(config, page, perPage, client))
      val id = (items \ "id").last.text.toInt

      if (id == lastId) lastCount
      else count(id, lastCount + items.length, page + 1)
    }
    count(Integer.MIN_VALUE, 0, 1)
  }

  private def receiveTickets(config: Map[String, String], client: SoapClient): Stream[Elem] = {
    val perPage = 20

    def nextPage(pageNr: Int, lastId: Int): Stream[Elem] = {
      debug("Laizily fetching next "+perPage+" items")
      val page = getPage(config, pageNr, perPage, client)
      val items = page \ "mc_project_get_issuesResponse" \ "return" \ "item" filter (_.isInstanceOf[Elem])
      val id = (items \ "id").last.text.toInt

      if (id == lastId) {
        Stream.empty
      } else {
        toStream(items, nextPage(pageNr + 1, id))
      }
    }

    nextPage(1, Integer.MIN_VALUE)
  }

  private def toStream(s: NodeSeq, next: => Stream[Elem]): Stream[Elem] =
    if (s.isEmpty) next
    else s.head.asInstanceOf[Elem] #:: toStream(s.tail, next)

  private def getPage(config: Map[String, String], page: Int, perPage: Int, client: SoapClient) =
    client.sendMessage(
      <mc:mc_project_get_issues xmlns:mc="http://futureware.biz/mantisconnect" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        { getRequestContent(config, page, perPage, client) }
      </mc:mc_project_get_issues>) match {
        case SoapResult(r) => r
        case SoapError(t, m) => throw new RuntimeException("Error ("+t+") while listing tickets:"+m)
      }

  private def getTicketHeaders(config: Map[String, String], page: Int, perPage: Int, client: SoapClient) =
    client.sendMessage(
      <mc:mc_project_get_issue_headers xmlns:mc="http://futureware.biz/mantisconnect" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        { getRequestContent(config, page, perPage, client) }
      </mc:mc_project_get_issue_headers>) match {
        case SoapResult(r) => r
        case SoapError(t, m) => throw new RuntimeException("Error ("+t+") while listing ticket header:"+m)
      }

  private def getRequestContent(config: Map[String, String], page: Int, perPage: Int, client: SoapClient) = Seq(
    <username xsi:type="xsd:string">{ config("username") }</username>,
    <password xsi:type="xsd:string">{ config("password") }</password>,
    <project_id xsi:type="xsd:integer">{ config("project").toInt }</project_id>,
    <page_number xsi:type="xsd:integer">{ page }</page_number>,
    <per_page xsi:type="xsd:integer">{ perPage }</per_page>)
}