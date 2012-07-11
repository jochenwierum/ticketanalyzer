package de.jowisoftware.mining.importer.trac

import scala.swing.{ Panel, GridPanel }

import de.jowisoftware.mining.UserOptions

class TracOptions extends UserOptions {
  protected var result: Map[String, String] = Map(
    ("url" -> "http://jowisoftware.de/trac/test/login/xmlrpc"),
    ("username" -> "test"), ("password" -> "test"), ("repositoryname" -> "default"))

  def getHtmlDescription = """<p><b>Trac Importer</b><br />
    Import tickets from a trac instance. To be able to do so:
    </p><ul>
      <li>Trac must have the TracXMLRPC plugin</li>
      <li>The user needs XML_RPC permissions</li>
      <li>Using the TracMasterTickets plugin<br />
        is recommendet</li>
    </ul>"""

  def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Url", text("url"))
    panel.add("User", text("username"))
    panel.add("Password", password("password"))
    panel.add("Repository name", text("repositoryname"))
  }
}