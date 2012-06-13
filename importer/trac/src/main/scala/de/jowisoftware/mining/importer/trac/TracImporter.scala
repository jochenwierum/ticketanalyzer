package de.jowisoftware.mining.importer.trac

import java.io.OutputStreamWriter
import java.net.{ URL, PasswordAuthentication, Authenticator }
import java.sql.Date

import scala.annotation.tailrec
import scala.xml.{ XML, NodeSeq, Elem }

import org.joda.time.format.DateTimeFormat

import de.jowisoftware.mining.importer.TicketDataFields._
import de.jowisoftware.mining.importer.TicketCommentDataFields
import de.jowisoftware.mining.importer.{ TicketUpdate, TicketData, TicketCommentData, Importer, ImportEvents }

class TracImporter extends Importer {
  private val dateFormat = DateTimeFormat.forPattern("yyyyMMdd'T'HH:mm:ss")

  def userOptions = new TracOptions()

  def importAll(config: Map[String, String], events: ImportEvents) {
    require(config.contains("url"))
    require(config.contains("username"))
    require(config.contains("password"))
    require(config.contains("repositoryname"))

    try {
      doImport(events, config)
    } finally {
      events.finish()
    }
  }

  private def doImport(events: ImportEvents, config: Map[String, String]) {
    setupAuth(config)

    val ticketlist = receiveTicketNumbers(config)
    val valueNodes = ticketlist \ "params" \ "param" \ "value" \ "array" \ "data" \ "value"
    val ticketIds = valueNodes.map { node => (node \ "int").text.toInt }
    events.countedTickets(ticketIds.size)
    ticketIds.foreach { tId =>
      events.loadedTicket(config("repositoryname"), List(getTicket(tId, config)), Seq())
    }
  }

  private def setupAuth(config: Map[String, String]) {
    Authenticator.setDefault(new Authenticator() {
      override def getPasswordAuthentication = new PasswordAuthentication(
        config("username"), config("password").toCharArray())
    })
  }

  private def receiveTicketNumbers(config: Map[String, String]) =
    retrieveXML(methodCall("ticket.query", <string>max=0</string>), config)

  private def getTicket(id: Int, config: Map[String, String]): TicketData = {
    val xml = receiveTicket(id, config)
    val values = xml \ "params" \ "param" \ "value" \ "array" \ "data" \ "value"
    val subValues = values \ "struct" \ "member";

    def findNode(name: String) =
      subValues.filter(node => (node \ "name").text == name) \ "value"

    val history = receiveHistory(id, config)
    val (ticketUpdates, comments) = getHistory(history)

    val ticketReporter = getNodeAsString(findNode("reporter"))
    val ticket = TicketData(getNodeAsInt(values(0)))
    ticket(creationDate) = (getNodeAsDate(values(1)) -> ticketReporter)
    ticket(updateDate) = (getNodeAsDate(values(2)) -> ticketReporter)
    ticket(status) = (getNodeAsString(findNode("status")) -> ticketReporter)
    ticket(description) = (getNodeAsString(findNode("description")) -> ticketReporter)
    ticket(reporter) = (getNodeAsString(findNode("reporter")) -> ticketReporter)
    ticket(resolution) = (getNodeAsString(findNode("resolution")) -> ticketReporter)
    ticket(component) = (getNodeAsString(findNode("component")) -> ticketReporter)
    ticket(tags) = (getNodeAsString(findNode("keywords")).split(" ").toSeq -> ticketReporter)
    //ticket(blocking) = (getNodeAsString(findNode("blocking")) -> ticketReporter)
    ticket(priority) = (getNodeAsString(findNode("priority")) -> ticketReporter)
    ticket(summary) = (getNodeAsString(findNode("summary")) -> ticketReporter)
    ticket(ticketType) = (getNodeAsString(findNode("type")) -> ticketReporter)
    ticket(owner) = (getNodeAsString(findNode("owner")) -> ticketReporter)
    ticket(milestone) = (getNodeAsString(findNode("milestone")) -> ticketReporter)
    ticket(version) = (getNodeAsString(findNode("version")) -> ticketReporter)
    //ticket(updates) = (ticketUpdates -> ticketReporter)

    ticket
  }

  private def getHistory(history: Elem) = {
    var result: List[TicketUpdate] = List()
    var comments: List[TicketCommentData] = List()

    val entries = history \ "params" \ "param" \ "value" \ "array" \ "data" \
      "value" \ "array" \ "data"

    entries.view.zipWithIndex.foreach {
      case (entry, id) =>
        val value = entry \ "value"
        var subResult: Map[String, Any] = Map()

        val time = getNodeAsDate(value(0))
        val author = getNodeAsString(value(1))
        val field = getNodeAsString(value(2))
        val oldValue = getNodeAsString(value(3))
        val newValue = getNodeAsString(value(4))

        if (field != "comment")
          result = TicketUpdate(id, field, newValue, oldValue, author, time) :: result
        else {
          val comment = new TicketCommentData()
          comment(TicketCommentDataFields.id) = oldValue.toInt -> author
          comment(TicketCommentDataFields.text) = newValue -> author
          comment(TicketCommentDataFields.author) = author -> author
          comment(TicketCommentDataFields.created) = time -> author
          comment(TicketCommentDataFields.modified) = time -> author
          comments = comment :: comments
        }
    }

    (result.reverse, comments.reverse)
  }

  private def getNodeAsDate(parent: NodeSeq, default: Date = null) = {
    val value = getTypedContent(parent, "dateTime.iso8601", null)
    if (value != null)
      dateFormat.parseDateTime(value).toDate()
    else
      default
  }

  private def getNodeAsInt(parent: NodeSeq, default: String = "0") =
    getTypedContent(parent, "int", default).toInt

  private def getNodeAsString(parent: NodeSeq, default: String = "") =
    getTypedContent(parent, "string", default)

  private def getTypedContent(parent: NodeSeq, expectedType: String, default: String) = {
    if (parent.isEmpty)
      default
    else {
      val node = parent \ expectedType
      require(!node.isEmpty, "Node '"+parent+"' did not yield the expected type: "+expectedType)
      node.text
    }
  }

  private def receiveTicket(id: Int, config: Map[String, String]) =
    retrieveXML(methodCall("ticket.get", <int>{ id }</int>), config)

  private def receiveHistory(id: Int, config: Map[String, String]) =
    retrieveXML(methodCall("ticket.changeLog", <int>{ id }</int>, <int>0</int>), config)

  private def methodCall(name: String, params: Elem*) =
    <methodCall>
      <methodName>{ name }</methodName>
      <params>
        <param>
          { params.map { x => <param>{ x }</param> } }
        </param>
      </params>
    </methodCall>

  def retrieveXML(request: Elem, config: Map[String, String]) = {
    val rpcurl = new URL(config("url"))
    val data = request.toString()
    val connection = rpcurl.openConnection()

    sendRequest(data, connection)
    XML.load(connection.getInputStream())
  }

  private def sendRequest(data: java.lang.String, connection: java.net.URLConnection): Unit = {
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/xml")
    val writer = new OutputStreamWriter(connection.getOutputStream())
    writer.write(data)
    writer.close
  }
}