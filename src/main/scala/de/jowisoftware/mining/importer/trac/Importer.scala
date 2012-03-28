package de.jowisoftware.mining.importer.trac
import java.net.{URL, URLEncoder}
import java.io.OutputStreamWriter
import scala.io.Source
import scala.xml.XML
import scala.xml.Elem
import java.net.Authenticator
import java.net.PasswordAuthentication
import scala.xml.NodeSeq

class Importer {
  private val rpcurl = "http://jowisoftware.de/trac/ssh/login/xmlrpc"
  
  def importAll() {
    setupAuth
    
    val ticketlist = receiveTicketNumbers
    val valueNodes = ticketlist \ "params" \ "param" \ "value" \ "array" \ "data" \ "value"
    val ticketIds = valueNodes.map {node => (node \ "int").text.toInt}
    
    //val ticketIds = List(27)
    val tickets = ticketIds.map(getTicket(_))
    println(tickets)
  }
  
  def setupAuth() {
    Authenticator.setDefault(new Authenticator() {
      override def getPasswordAuthentication = new PasswordAuthentication("test", "test".toCharArray())
    })
  }

  def receiveTicketNumbers =
    retrieveXML(<methodCall>
        <methodName>ticket.query</methodName>
        <params>
          <param>
            <param><string>max=0</string></param>
          </param>
        </params>
        </methodCall>)

  def getTicket(id: Int) = {
    val xml = receiveTicket(id)
    
    val body = xml \ "params" \ "param" \ "value" \ "array" \ "data"
    val values = body \ "value"
    
    var result: Map[String, (String, String)] = Map()
    result += wrap("id", values.head)
    result += wrap("creationDate", values.tail.head)
    result += wrap("updateDate", values.drop(2).head)
    
    (values \ "struct" \ "member").foreach {member =>
      result += wrap((member \ "name").text, member \ "value")
    }
    
    result
  }
  
  private def wrap(name: String, seq: NodeSeq) = seq.head.child.head match {
  case e: Elem =>
    (name -> (e.label -> e.text))
  } 

  private def receiveTicket(id: Int) =
    retrieveXML(<methodCall>
        <methodName>ticket.get</methodName>
        <params>
          <param><int>{id}</int></param>
        </params>
        </methodCall>)
        
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