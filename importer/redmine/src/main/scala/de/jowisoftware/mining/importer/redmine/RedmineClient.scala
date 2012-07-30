package de.jowisoftware.mining.importer.redmine

import java.net.URLEncoder
import java.net.URL
import scala.xml.XML
import scala.annotation.tailrec
import scala.collection.mutable
import scala.xml.Elem

import de.jowisoftware.util.XMLUtils._

class RedmineClient(redmineUrl: String, apikey: String) {
  private val baseUrl = redmineUrl + (if (!redmineUrl.endsWith("/")) "/" else "")

  def retrieveXML(file: String, parameterMap: Map[String, String]) = {
    def encode(s: String) = URLEncoder.encode(s, "UTF-8")
    val params = parameterMap map { case (key, value) => encode(key)+"="+encode(value) } mkString "&"

    val rpcUrl = new URL(baseUrl + file +
      (if (params.isEmpty) "" else "?"+params))

    val connection = rpcUrl.openConnection()
    connection.setRequestProperty("Content-Type", "application/xml")
    connection.setRequestProperty("X-Redmine-API-Key", apikey)
    XML.load(connection.getInputStream())
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