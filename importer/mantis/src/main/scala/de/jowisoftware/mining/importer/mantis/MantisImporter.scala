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

object MantisImporter {
  private val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  private def toDate(value: String) = dateFormat.parseDateTime(value).toDate()

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

    val client = new SoapClient

    processAllTickets(config, client, events)
  }

  private def processAllTickets(config: Map[String, String], client: SoapClient, events: ImportEvents) {
    info("Counting tickets...")
    events.countedTickets(countTickets(config, client))

    info("Importing tickets...")
    val items = receiveTickets(config, client)
    items foreach { t => processTicket(t, events, config("repositoryname")) }
    info("Importing finished.")
  }

  private def processTicket(item: Elem, events: ImportEvents, repository: String) {
    def subnode(name: String) = item \ name \ "name" text
    def node(name: String) = item \ name text

    val reproducibility = subnode("reproducibility")
    val build = node("build")
    val handler = subnode("handler")
    val eta = subnode("eta")
    //val relationships =

    val ticket = new TicketData(repository, node("id").toInt,
      summary = node("summary"),
      description = node("description")+"\n"+node("steps_to_reproduce")+"\n"+node("additional_information"),
      creationDate = toDate(node("date_submitted")),
      updateDate = toDate(node("last_updated")),
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

    events.loadedTicket(ticket)
    //println(new PrettyPrinter(120, 2).format(item))
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
            submitted = toDate(comment \ "date_submitted" text),
            modified = toDate(comment \ "last_modified" text)))
        } else {
          None
        }
      case _ => None
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
    client.sendMessage(config("url"),
      <mc:mc_project_get_issues xmlns:mc="http://futureware.biz/mantisconnect" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        { getRequestContent(config, page, perPage, client) }
      </mc:mc_project_get_issues>) match {
        case SoapResult(r) => r
        case SoapError(t, m) => throw new RuntimeException("Error ("+t+") while listing tickets:"+m)
      }

  private def getTicketHeaders(config: Map[String, String], page: Int, perPage: Int, client: SoapClient) =
    client.sendMessage(config("url"),
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