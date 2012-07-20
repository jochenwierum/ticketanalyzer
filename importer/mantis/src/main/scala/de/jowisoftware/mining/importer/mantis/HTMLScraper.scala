package de.jowisoftware.mining.importer.mantis

import scala.xml.Node
import scala.io.Source
import scala.collection.JavaConversions._
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.URLEncoder
import java.net.HttpURLConnection
import java.net.URLConnection
import java.net.URL

private[mantis] class HTMLScraper(dirtyUrl: String) {
  private var cookies: Map[String, String] = Map()
  private val url = if (dirtyUrl.endsWith("/")) dirtyUrl else (dirtyUrl + "/")

  def login(user: String, password: String) =
    post("login.php", Map("username" -> user, "password" -> password))

  def logout() =
    get("login_page.php")

  def readTicket(id: Int) =
    get("view.php?id="+id)

  private def get(file: String) = {
    val conn = getConnection(file)
    processRequest(conn)
  }

  private def post(file: String, data: Map[String, String]) = {
    val conn = getConnection(file)

    conn.setDoOutput(true)
    val writer = new OutputStreamWriter(conn.getOutputStream)
    val postData = data.map { case (k, v) => k+"="+URLEncoder.encode(v, "UTF-8") }.mkString("&")
    writer.write(postData)
    writer.close()

    processRequest(conn)
  }

  private def processRequest(conn: URLConnection): scala.xml.Node = {
    val cookieHeader = conn.getHeaderField("Set-Cookie")
    if (cookieHeader != null) {
      cookies ++= cookieHeader.split("\n").map { cookie =>
        val parts = cookie.split("=", 2)
        val value = parts(1).split(";")(0)

        (parts(0), value)
      }
    }

    val redirect = conn.getHeaderField("Location")
    if (redirect != null) {
      val cleanURL = if (redirect.startsWith(url)) {
        redirect.substring(url.length)
      } else redirect
      get(cleanURL)
    } else {
      new HTML5Parser().loadXML(conn.getInputStream)
    }
  }

  private def getConnection(file: String): URLConnection = {
    val callUrl = new java.net.URL(url + file)
    val conn = callUrl.openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestProperty("Cookie", cookies.map { case (k, v) => k+"="+v }.mkString(";"))
    conn.setInstanceFollowRedirects(false)
    conn
  }
}