package de.jowisoftware.mining.importer.redmine

import de.jowisoftware.mining.importer.Importer
import de.jowisoftware.mining.importer.ImportEvents
import java.net.URL
import java.net.URLConnection
import java.io.OutputStreamWriter
import scala.xml.XML
import java.net.URLEncoder
import de.jowisoftware.util.XMLUtils._

class RedmineImporter extends Importer {
  def userOptions = new RedmineOptions()

  def importAll(config: Map[String, String], events: ImportEvents) {
    println(retrieveXML("issues.xml", Map(), config).formatted)
  }

  def retrieveXML(file: String, request: Map[String, String], config: Map[String, String]) = {
    def encode(s: String) = URLEncoder.encode(s, "UTF-8")
    val params = request map { case (key, value) => encode(key)+"="+encode(value) } mkString "&"

    val rpcUrl = new URL(config("url") +
        (if (!config("url").endsWith("/")) "/" else "") +
        file +
        (if (params.isEmpty) "" else "?"+params))

    val connection = rpcUrl.openConnection()
    sendRequest(connection, config("key"))
    XML.load(connection.getInputStream())
  }

  private def sendRequest(connection: URLConnection, key: String): Unit = {
    connection.setRequestProperty("Content-Type", "application/xml")
    connection.setRequestProperty("X-Redmine-API-Key", key)
  }
}