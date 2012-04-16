package de.jowisoftware.mining.importer.trac
import java.io.OutputStreamWriter
import java.net.{ URL, PasswordAuthentication, Authenticator }
import scala.xml.{ XML, NodeSeq, Elem }
import org.joda.time.format.DateTimeFormat
import de.jowisoftware.mining.importer.{ Importer, ImportEvents }
import scala.annotation.tailrec
import de.jowisoftware.mining.importer.TicketData
import java.sql.Date
import de.jowisoftware.mining.importer.TicketUpdate

class TracImporter extends Importer {
  private val dateFormat = DateTimeFormat.forPattern("yyyyMMdd'T'HH:mm:ss")

  def showOptions = new TracOptions()

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
    ticketIds.foreach(tId => events.loadedTicket(getTicket(tId, config)))
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

    val updates = getHistory(id, config)
    TicketData(
      repository = config("repositoryname"),
      id = getNodeAsInt(values(0)),
      creationDate = getNodeAsDate(values(1)),
      updateDate = getNodeAsDate(values(2)),
      status = getNodeAsString(findNode(subValues, "status")),
      description = getNodeAsString(findNode(subValues, "description")),
      reporter = getNodeAsString(findNode(subValues, "reporter")),
      resolution = getNodeAsString(findNode(subValues, "resolution")),
      component = getNodeAsString(findNode(subValues, "component")),
      tags = getNodeAsString(findNode(subValues, "keywords")),
      blocking = getNodeAsString(findNode(subValues, "blocking")),
      priority = getNodeAsString(findNode(subValues, "priority")),
      summary = getNodeAsString(findNode(subValues, "summary")),
      ticketType = getNodeAsString(findNode(subValues, "type")),
      owner = getNodeAsString(findNode(subValues, "owner")),
      milestone = getNodeAsString(findNode(subValues, "milestone")),
      version = getNodeAsString(findNode(subValues, "version")),
      updates = updates)
  }

  private def getHistory(id: Int, config: Map[String, String]) = {
    var result: List[TicketUpdate] = List()
    val history = receiveHistory(id, config)
    val entries = history \ "params" \ "param" \ "value" \ "array" \ "data" \
      "value" \ "array" \ "data"

    entries.view.zipWithIndex.foreach {
      case (entry, id) =>
        val value = entry \ "value"
        var subResult: Map[String, Any] = Map()

        val time = getNodeAsDate(value(0))
        val author = getNodeAsString(value(1))
        val field = getNodeAsString(value(2))
        val oldvalue = getNodeAsString(value(3))
        val newvalue = getNodeAsString(value(4))
        result = TicketUpdate(id, field, newvalue, oldvalue, author, time) :: result
    }

    result.reverse
  }

  private def findNode(parent: NodeSeq, name: String) =
    parent.filter(node => (node \ "name").text == name) \ "value"

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