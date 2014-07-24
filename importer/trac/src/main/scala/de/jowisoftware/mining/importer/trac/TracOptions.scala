package de.jowisoftware.mining.importer.trac

import de.jowisoftware.mining.UserOptions

class TracOptions extends UserOptions("importer.trac") {
  protected val defaultResult: Map[String, String] = Map(
    "url" -> "http://example.org/mytrac",
    "username" -> "",
    "password" -> "",
    "repositoryname" -> "default")

  protected val htmlDescription = """<p><b>Trac Importer</b><br />
    Import tickets from a trac instance. To be able to do so:
    </p><ul>
      <li>Trac must have the TracXMLRPC plugin</li>
      <li>The user needs XML_RPC permissions</li>
      <li>Using the TracMasterTickets plugin<br />
        is recommendet</li>
    </ul>"""

  protected def fillPanel(panel: CustomizedGridBagPanel): Unit = {
    panel.add("Url", text("url"))
    panel.add("User", text("username"))
    panel.add("Password", password("password"))
    panel.add("Repository name", text("repositoryname"))
  }
}
