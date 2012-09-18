package de.jowisoftware.mining.importer.redmine

import java.io.{ IOException, InputStream }
import java.net.{ URL, URLConnection, URLEncoder }

import scala.annotation.tailrec
import scala.xml.{ Elem, XML }

import de.jowisoftware.util.XMLUtils.NodeSeq2EnrichedNodeSeq
import grizzled.slf4j.Logging

class RedmineClient(redmineUrl: String, apikey: String) extends Logging {
  private val baseUrl = redmineUrl + (if (!redmineUrl.endsWith("/")) "/" else "")

  def retrieveXML(file: String, parameterMap: Map[String, String]) = {
    def encode(s: String) = URLEncoder.encode(s, "UTF-8")
    val params = parameterMap map { case (key, value) => encode(key)+"="+encode(value) } mkString "&"

    val rpcUrl = new URL(baseUrl + file +
      (if (params.isEmpty) "" else "?"+params))

    val connection = rpcUrl.openConnection()
    connection.setRequestProperty("Content-Type", "application/xml")
    connection.setRequestProperty("X-Redmine-API-Key", apikey)

    XML.load(openInputStream(connection))
  }

  private def openInputStream(connection: URLConnection): InputStream = {
    for (i <- 0 to 2) {
      try {
        val stream = connection.getInputStream()
        return stream
      } catch {
        case e: IOException =>
          warn("IOException while receiving page... retrying "+(i + 1), e)
          Thread.sleep(2000)
      }
    }
    throw new RuntimeException("Could not open connection to "+connection.getURL().toString())
  }

  def retrivePagedXML(file: String,
    parameterMap: Map[String, String] = Map(),
    processor: Elem => Unit) {

    readPaged(file, parameterMap, processor, 0)
  }

  @tailrec private def readPaged(file: String,
    parameterMap: Map[String, String] = Map(),
    processor: Elem => Unit, start: Int) {

    val page = retrieveXML(file, parameterMap ++ Map(
      "offset" -> start.toString,
      "limit" -> "25"))

    processor(page)

    val isPaged = (page \ "@limit").length > 0
    val total = if (isPaged) (page \ "@total_count" intText) else 0
    val newOffset = if (isPaged) ((page \ "@offset" intText) + (page \ "@limit" intText)) else 0

    if (newOffset < total) {
      readPaged(file, parameterMap, processor, newOffset)
    }
  }
}