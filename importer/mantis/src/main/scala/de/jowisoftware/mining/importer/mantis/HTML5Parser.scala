package de.jowisoftware.mining.importer.mantis

import org.xml.sax.InputSource
import scala.xml._
import parsing._
import java.net.URL
import java.io.InputStream

private[mantis] class HTML5Parser extends NoBindingFactoryAdapter {
  override def loadXML(source: InputSource, _p: SAXParser) = {
    loadXML(source)
  }

  def loadXML(text: String): Node =
    loadXML(new InputSource(text))

  def loadXML(is: InputStream): Node =
    loadXML(new InputSource(is))

  def loadXML(source: InputSource): Node = {
    import nu.validator.htmlparser.{ sax, common }
    import sax.HtmlParser
    import common.XmlViolationPolicy

    val reader = new HtmlParser
    reader.setXmlPolicy(XmlViolationPolicy.ALLOW)
    reader.setContentHandler(this)
    reader.parse(source)
    rootElem
  }
}