package de.jowisoftware.mining.importer.mantis

import de.jowisoftware.mining.importer.Importer
import scala.collection.immutable.Map
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.importer.ImportEvents
import scala.xml.Elem
import scala.xml.PrettyPrinter
import org.joda.time.format.DateTimeFormat
import de.jowisoftware.mining.model.Ticket
import de.jowisoftware.mining.importer.TicketData
import de.jowisoftware.mining.importer.TicketComment
import scala.xml.NodeSeq
import grizzled.slf4j.Logging
import scala.annotation.tailrec
import scala.xml.XML
import scala.io.Source
import java.util.Date
import scala.collection.SortedMap
import de.jowisoftware.mining.importer.TicketData

object MantisImporter {
  private val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  private def fromComplexDate(value: String) = dateFormat.parseDateTime(value).toDate()

  private val simpleDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
  private def fromSimpleDate(value: String) = simpleDateFormat.parseDateTime(value).toDate()

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
    items foreach { t => processTicket(t, events, config("repositoryname"), scraper) }

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
    /*
    if (node("id").toInt == 1)
      println(new PrettyPrinter(120, 2).format(item))
      */
  }

  private def createTicket(item: scala.xml.Elem, repository: String, id: Int) = {
    def subnode(name: String) = item \ name \ "name" text
    def node(name: String) = item \ name text

    val reproducibility = subnode("reproducibility")
    val build = node("build")
    val handler = subnode("handler")
    val eta = subnode("eta")

    new TicketData(repository, id,
      summary = node("summary"),
      description = node("description")+"\n"+node("steps_to_reproduce")+"\n"+node("additional_information"),
      creationDate = fromComplexDate(node("date_submitted")),
      updateDate = fromComplexDate(node("last_updated")),
      version = node("version"),
      status = subnode("status"),
      priority = subnode("priority"),
      reporter = subnode("reporter"),
      component = node("category"),
      resolution = subnode("resolution"),
      severity = subnode("severity"),
      fixedInVersion = subnode("fixed_in_version"),
      comments = comments(item \ "notes"),
      votes = node("sponsorship_total").toInt,
      environment = node("platform")+" "+node("os")+" "+node("osBuild"))
  }

  private def comments(item: NodeSeq) =
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
    changes.map {
      case (timestamp, changes) =>
        println(timestamp)
    }
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