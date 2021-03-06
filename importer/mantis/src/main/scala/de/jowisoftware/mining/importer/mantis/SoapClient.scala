package de.jowisoftware.mining.importer.mantis

import scala.xml._
import grizzled.slf4j.Logging

class SoapClient(host: String) extends Logging {
  private val url = new java.net.URL(host + (if (host.endsWith("/")) "" else "/")+"api/soap/mantisconnect.php")

  private def wrap(xml: Elem): String = {
    val buf = new StringBuilder
    buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
    buf.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n")
    buf.append("<SOAP-ENV:Body>\n")
    buf.append(xml.toString)
    buf.append("\n</SOAP-ENV:Body>\n")
    buf.append("</SOAP-ENV:Envelope>\n")
    buf.toString
  }

  private def processAnswer(answer: Elem): SoapResult = {
    trace(new PrettyPrinter(120, 2).format(answer))

    val body = answer \ "Body"
    if (!body.isEmpty)
      SoapResult(body(0).asInstanceOf[Elem])
    else
      throw new IllegalStateException("Result did not contain body: "+answer)
  }

  private def processError(error: Elem): SoapError = {
    trace(new PrettyPrinter(120, 2).format(error))

    val text = (error \ "Body" \ "Fault" \ "faultstring").text
    val code = (error \ "Body" \ "Fault" \ "faultcode").text
    SoapError(code, text)
  }

  def sendMessage(req: Elem): SoapResponse = {
    val out = wrap(req)
    val conn = url.openConnection.asInstanceOf[java.net.HttpURLConnection]

    trace("Sending: "+out)

    try {
      conn.setRequestMethod("POST")
      val outs = out.getBytes
      conn.setDoOutput(true)
      conn.setRequestProperty("Content-Length", outs.length.toString)
      conn.setRequestProperty("Content-Type", "text/xml")
      val stream = conn.getOutputStream
      stream.write(outs)
      stream.close
      val result = processAnswer(XML.load(conn.getInputStream))
      conn.disconnect
      result
    } catch {
      case e: Exception =>
        warn("Error when communicating to "+host, e)
        processError(XML.load(conn.getErrorStream()))
    }
  }
}