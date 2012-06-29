package de.jowisoftware.mining.importer.mantis

import java.util.Date

import scala.Option.option2Iterable
import scala.annotation.tailrec
import scala.collection.immutable.Stream.consWrapper
import scala.collection.immutable.Map
import scala.collection.SortedMap
import scala.xml.{ NodeSeq, Elem }

import org.joda.time.format.DateTimeFormat

import MantisImporter.{ fromComplexDate, MantisConstants }
import de.jowisoftware.mining.importer.TicketDataFields._
import de.jowisoftware.mining.importer._
import de.jowisoftware.mining.UserOptions
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
    items foreach { t => processTicket(t, events, config("repositoryname"), scraper) }

    scraper.logout
    info("Importing finished.")
  }

  private def processTicket(item: Elem, events: ImportEvents, repository: String, scraper: HTMLScraper) {
    val allComments = getComments(item \ "notes")
    val ticket = createTicket(item, allComments)

    val html = scraper.readTicket(ticket(id))
    val historyTable = (html \\ "div" filter { tag => (tag \ "@id").text == "history_open" })(0)
    val historyRows = historyTable \\ "tr" drop 2

    val changeParser = new ChangeParser
    val changes = historyRows.flatMap(row => changeParser.parse(row, ticket))
    val changesByDate = SortedMap.empty[Date, Seq[Change]] ++ changes.groupBy(_.date)

    val baseTicket = createBaseTicket(ticket, changesByDate)
    val allTickets = createTicketUpdates(baseTicket, changesByDate)

    events.loadedTicket(repository, allTickets, allComments)
  }

  private def createTicket(item: Elem, allComments: Seq[TicketCommentData]) = {
    def subnode(name: String) = item \ name \ "name" text
    def node(name: String) = item \ name text

    val reporterName = subnode("reporter")

    val ticket = TicketData((item \ "id").text.toInt)
    ticket(summary) = node("summary")
    ticket(description) = (node("description")+"\n"+node("steps_to_reproduce")+"\n"+node("additional_information"))
    ticket(creationDate) = fromComplexDate(node("date_submitted"))
    ticket(updateDate) = fromComplexDate(node("last_updated"))
    ticket(version) = node("version")
    ticket(build) = node("build")
    ticket(status) = subnode("status")
    ticket(priority) = subnode("priority")
    ticket(reporter) = subnode("reporter")
    ticket(component) = node("category")
    ticket(resolution) = subnode("resolution")
    ticket(severity) = subnode("severity")
    ticket(fixedInVersion) = subnode("fixed_in_version")
    ticket(fixedInVersion) = subnode("fixed_in_version")
    ticket(comments) = allComments.map(_(TicketCommentDataFields.id))
    ticket(votes) = node("sponsorship_total").toInt
    ticket(environment) = (node("platform")+":"+node("os")+":"+node("osBuild"))
    ticket(eta) = ValueUtils.etaStringToInt(subnode("eta"))
    ticket(reproducability) = subnode("reproducability")
    ticket(owner) = subnode("handler")
    ticket(relationships) = getRelationships(item \ "relationships")

    ticket
  }

  private def createBaseTicket(ticket: TicketData, changes: SortedMap[Date, Seq[Change]]) = {
    val reversedChanges = SortedMap.empty(changes.ordering.reverse) ++ changes.toList
    reversedChanges.foreach {
      _._2.foreach {
        _.downgrade(ticket)
      }
    }

    ticket(updateDate) = ticket(creationDate)
    ticket(editor) = Some(reversedChanges.valuesIterator.next()(0).editor)
    ticket
  }

  private def createTicketUpdates(baseTicket: TicketData, changes: SortedMap[Date, Seq[Change]]) = {
    def nextTickets(ticket: TicketData, changes: List[(Date, Seq[Change])]): List[TicketData] = changes match {
      case Nil => Nil
      case head :: tail =>
        val newTicket = new TicketData(ticket)
        head._2.foreach(_.update(newTicket))
        newTicket(editor) = Some(head._2(0).editor)
        newTicket(updateDate) = head._1
        newTicket :: nextTickets(newTicket, tail)
    }

    baseTicket :: nextTickets(baseTicket, changes.toList)
  }

  private def getComments(item: NodeSeq) = (item \ "item").flatMap {
    case commentNode: Elem =>
      val public = (commentNode \ "view_state" \ "id" text).toInt == MantisConstants.public
      if (public) {
        val comment = new TicketCommentData()
        val author = (commentNode \ "reporter" \ "name").text
        comment(TicketCommentDataFields.id) = (commentNode \ "id" text).toInt
        comment(TicketCommentDataFields.text) = (commentNode \ "text").text
        comment(TicketCommentDataFields.created) = fromComplexDate(commentNode \ "date_submitted" text)
        comment(TicketCommentDataFields.modified) = fromComplexDate(commentNode \ "last_modified" text)
        Some(comment)
      } else {
        None
      }
    case _ => None
  }

  private def getRelationships(item: NodeSeq) = (item \ "item").flatMap {
    case relationship: Elem =>
      val typeString = relationship \ "type" \ "name" text
      val target = (relationship \ "target_id" text) toInt

      ValueUtils.relationshipStringToRelationshipType(typeString) match {
        case Some(relValue) => Some(new TicketRelationship(target.toInt, relValue))
        case None => None
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
      debug("Lazily fetching next "+perPage+" items")
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