package de.jowisoftware.mining.importer.trac

import java.io.OutputStreamWriter
import java.net.{ URLConnection, URL, PasswordAuthentication, Authenticator }
import java.util.Date
import scala.collection.SortedMap
import scala.xml.{ XML, NodeSeq, Elem }
import org.joda.time.format.DateTimeFormat
import de.jowisoftware.mining.importer.TicketDataFields._
import de.jowisoftware.mining.importer._
import de.jowisoftware.util.XMLUtils

object TracImporter {
  private val dateFormat = DateTimeFormat.forPattern("yyyyMMdd'T'HH:mm:ss")
}

class TracImporter(config: Map[String, String], events: ImportEvents) {
  case class Comment(time: Date, user: String, text: String)

  require(config.contains("url"))
  require(config.contains("username"))
  require(config.contains("password"))
  require(config.contains("repositoryname"))

  def run() {
    try {
      setupAuth()
      doImport()
    } finally {
      events.finish()
    }
  }

  private def doImport() {
    val ticketlist = receiveTicketNumbers()
    val valueNodes = ticketlist \ "params" \ "param" \ "value" \ "array" \ "data" \ "value"
    val ticketIds = valueNodes.map { node => (node \ "int").text.toInt }
    events.countedTickets(ticketIds.size)
    ticketIds.foreach { tId =>
      val (tickets, comments) = getTicket(tId)
      events.loadedTicket(config("repositoryname"), tickets, comments)
    }
  }

  private def setupAuth() {
    Authenticator.setDefault(new Authenticator() {
      override def getPasswordAuthentication = new PasswordAuthentication(
        config("username"), config("password").toCharArray())
    })
  }

  private def receiveTicketNumbers() =
    retrieveXML(methodCall("ticket.query", <string>max=0</string>))

  private def getTicket(id: Int): (List[TicketData], Seq[TicketCommentData]) = {
    val ticket = createTicket(id)
    val (comments, rawHistory) = parseHistory(receiveHistory(id), ticket)
    val history = groupHistory(rawHistory)
    val baseTicket = createBaseTicket(ticket, history)
    val tickets = createTicketUpdates(baseTicket, history)

    (tickets, comments)
  }

  private def createTicket(id: Int): TicketData = {
    val xml = receiveTicket(id)
    val values = xml \ "params" \ "param" \ "value" \ "array" \ "data" \ "value"
    val subValues = values \ "struct" \ "member"

    def findNode(name: String) =
      subValues.filter(node => (node \ "name").text == name) \ "value"

    val ticket = TicketData(getNodeAsInt(values(0)))
    ticket(creationDate) = getNodeAsDate(values(1))
    ticket(updateDate) = getNodeAsDate(values(2))
    ticket(startDate) = ticket(creationDate)
    ticket(status) = getNodeAsString(findNode("status"))
    ticket(description) = getNodeAsString(findNode("description"))
    ticket(reporter) = getNodeAsString(findNode("reporter"))
    ticket(resolution) = getNodeAsString(findNode("resolution"))
    ticket(component) = getNodeAsString(findNode("component"))
    ticket(tags) = getNodeAsString(findNode("keywords")).split(' ').toSeq.sorted
    ticket(priority) = getNodeAsString(findNode("priority"))
    ticket(summary) = getNodeAsString(findNode("summary"))
    ticket(ticketType) = getNodeAsString(findNode("type"))
    ticket(owner) = getNodeAsString(findNode("owner"))
    ticket(milestone) = getNodeAsString(findNode("milestone"))
    ticket(version) = getNodeAsString(findNode("version"))
    ticket(reporter) = getNodeAsString(findNode("reporter"))
    ticket
  }

  private def parseHistory(history: Elem, ticket: TicketData) = {
    val entries = history \ "params" \ "param" \ "value" \ "array" \ "data" \
      "value" \ "array" \ "data"
    val changeParser = new ChangeParser

    var comments: Map[Int, TicketCommentData] = Map()
    val changes = entries.zipWithIndex.flatMap {
      case (entry, id) =>
        val value = entry \ "value"

        val time = getNodeAsDate(value(0))
        val author = getNodeAsString(value(1))
        val field = getNodeAsString(value(2))
        val oldValue = getNodeAsString(value(3))
        val newValue = getNodeAsString(value(4))

        if (field == "comment") {
          if (newValue.isEmpty || oldValue.isEmpty) {
            None
          } else {
            val comment = changeParser.createComment(time, author, field, oldValue, newValue)
            comments += comment(TicketCommentDataFields.id) -> comment
            changeParser.wrapChange(time, author, field, "",
              comment(TicketCommentDataFields.id).toString, ticket)
          }
        } else {
          changeParser.wrapChange(time, author, field, oldValue, newValue, ticket)
        }
    }

    (comments.values.toSeq.sortBy(_(TicketCommentDataFields.id)), changes)
  }

  private def groupHistory(changes: Seq[Change]) =
    SortedMap.empty[Date, Seq[Change]] ++ changes.groupBy(_.date)

  private def createBaseTicket(ticket: TicketData, changes: SortedMap[Date, Seq[Change]]) = {
    val reversedChanges = SortedMap.empty(changes.ordering.reverse) ++ changes.toList
    reversedChanges.foreach {
      _._2.foreach {
        _.downgrade(ticket)
      }
    }

    ticket(updateDate) = ticket(creationDate)
    if (!reversedChanges.isEmpty) {
      ticket(editor) = Some(reversedChanges.valuesIterator.next()(0).editor)
    }
    ticket
  }

  private def createTicketUpdates(baseTicket: TicketData, changes: SortedMap[Date, Seq[Change]]) = {
    def nextTickets(ticket: TicketData, changes: List[(Date, Seq[Change])]): List[TicketData] = changes match {
      case Nil => Nil
      case head :: tail =>
        val newTicket = new TicketData(ticket)
        head._2.foreach(_.update(newTicket))
        newTicket(updateDate) = head._1
        newTicket(editor) = Some(head._2(0).editor)
        newTicket :: nextTickets(newTicket, tail)
    }

    baseTicket :: nextTickets(baseTicket, changes.toList)
  }

  private def getNodeAsDate(parent: NodeSeq, default: Date = null) = {
    val value = getTypedContent(parent, "dateTime.iso8601", null)
    if (value != null)
      TracImporter.dateFormat.parseDateTime(value).toDate()
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

  private def receiveTicket(id: Int) =
    retrieveXML(methodCall("ticket.get", <int>{ id }</int>))

  private def receiveHistory(id: Int) =
    retrieveXML(methodCall("ticket.changeLog", <int>{ id }</int>, <int>0</int>))

  private def methodCall(name: String, params: Elem*) =
    <methodCall>
      <methodName>{ name }</methodName>
      <params>
        <param>
          { params.map { x => <param>{ x }</param> } }
        </param>
      </params>
    </methodCall>

  def retrieveXML(request: Elem) = {
    val baseUrl = (if (config("url") endsWith "/") config("url") else config("url")+"/")
    val rpcurl = new URL(baseUrl+"login/xmlrpc")
    val data = request.toString()
    val connection = rpcurl.openConnection()

    sendRequest(data, connection)
    XML.load(connection.getInputStream())
  }

  private def sendRequest(data: String, connection: URLConnection) {
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/xml")
    val writer = new OutputStreamWriter(connection.getOutputStream())
    writer.write(data)
    writer.close
  }
}
