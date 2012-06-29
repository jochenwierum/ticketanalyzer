package de.jowisoftware.mining.importer.redmine

import java.net.{URLEncoder, URL}

import scala.annotation.tailrec
import scala.xml.{XML, Node}

import de.jowisoftware.mining.importer.{Importer, ImportEvents}
import de.jowisoftware.util.XMLUtils.XML2FormatableXML

class RedmineImporter extends Importer {
  def userOptions = new RedmineOptions()

  def importAll(config: Map[String, String], events: ImportEvents) {
    processAllTickets(config, events)
  }

  @tailrec private def processAllTickets(config: Map[String, String], events: ImportEvents, start: Int=0) {
    val page = retrieveXML("issues.xml",
        Map("offset" -> start.toString,
            "limit" -> "25",
            "project_id" -> config("project")),
        config)

    page \ "issue" foreach { node =>
      val xml = receiveTicket(node, config)
      processTicket(xml, config, events)
    }

    val total = (page \ "@total_count" text).toInt
    val newOffset = ((page \ "@offset" text).toInt + (page \ "@limit" text).toInt)

    if (newOffset < total) {
      processAllTickets(config, events, newOffset)
    }
  }

  def receiveTicket(ticketXML: Node, config: Map[String, String]) = {
    val id = (ticketXML \ "id" text).toInt

    retrieveXML("issues/" + id + ".xml", Map(
        "include" -> "children,attachments,relations,changesets,journals"),
        config)
  }

  def processTicket(ticketXML: Node, config: Map[String, String], events: ImportEvents) {
    println(ticketXML.formatted)
  }

  def retrieveXML(file: String, request: Map[String, String], config: Map[String, String]) = {
    def encode(s: String) = URLEncoder.encode(s, "UTF-8")
    val params = request map { case (key, value) => encode(key)+"="+encode(value) } mkString "&"

    val rpcUrl = new URL(config("url") +
        (if (!config("url").endsWith("/")) "/" else "") +
        file +
        (if (params.isEmpty) "" else "?"+params))

    val connection = rpcUrl.openConnection()
    connection.setRequestProperty("Content-Type", "application/xml")
    connection.setRequestProperty("X-Redmine-API-Key", config("key"))
    XML.load(connection.getInputStream())
  }
}