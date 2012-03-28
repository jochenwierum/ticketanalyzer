package de.jowisoftware.mining.importer.trac
import java.net.{URL, URLEncoder}
import java.io.OutputStreamWriter
import scala.io.Source
import scala.xml.XML
import scala.xml.Elem
import java.net.Authenticator
import java.net.PasswordAuthentication
import scala.xml.NodeSeq
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.format.DateTimeFormat

class Importer {
  private val rpcurl = "http://jowisoftware.de/trac/ssh/login/xmlrpc"
  private val dateFormat = DateTimeFormat.forPattern("yyyyMMdd'T'HH:mm:ss")
    
  def importAll() {
    setupAuth
    
    val ticketlist = receiveTicketNumbers
    val valueNodes = ticketlist \ "params" \ "param" \ "value" \ "array" \ "data" \ "value"
    val ticketIds = valueNodes.map {node => (node \ "int").text.toInt}
    
    //val ticketIds = List(27)
    val tickets = ticketIds.view.map(getTicket(_))
    println(tickets.force)
  }
  
  def setupAuth() {
    Authenticator.setDefault(new Authenticator() {
      override def getPasswordAuthentication = new PasswordAuthentication("test", "test".toCharArray())
    })
  }

  def receiveTicketNumbers =
    retrieveXML(methodCall("ticket.query", <string>max=0</string>))
        
  def getTicket(id: Int) = {
    val xml = receiveTicket(id)
    
    val body = xml \ "params" \ "param" \ "value" \ "array" \ "data"
    val values = body \ "value"
    
    var result: Map[String, Any] = Map()
    result += "id" -> unpack(values.head)
    result += "creationDate" -> unpack(values.tail.head)
    result += "updateDate" -> unpack(values.drop(2).head)
    
    (values \ "struct" \ "member").foreach {member =>
      result += (member \ "name").text -> unpack(member \ "value")
    }
    
    result
  }
  
  private def unpack(seq: NodeSeq): Any = seq.head.child.head match {
  case e: Elem =>
    e.label match {
      case "string" => e.text
      case "int" => e.text.toInt
      case "dateTime.iso8601" => dateFormat.parseDateTime(e.text).toDate()
    }
  } 

  private def receiveTicket(id: Int) =
    retrieveXML(methodCall("ticket.get", <int>{id}</int>))
    
  private def methodCall(name: String, params: Elem) =
    <methodCall>
      <methodName>{name}</methodName>
      <params>
        <param>
          {params.map {x => <param>{x}</param>}}
        </param>
      </params>
    </methodCall>
    
  def retrieveXML(request: Elem) = {
    val url = new URL(rpcurl)
    val data = request.toString()
    val connection = url.openConnection()
    
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