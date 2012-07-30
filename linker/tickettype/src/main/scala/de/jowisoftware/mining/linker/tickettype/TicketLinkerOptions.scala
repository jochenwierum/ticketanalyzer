package de.jowisoftware.mining.linker.tickettype

import de.jowisoftware.mining.UserOptions

class TicketLinkerOptions extends UserOptions {
  protected var result: Map[String, String] = Map()

  def getHtmlDescription() = """<p><b>Link Ticket Types</b><br />
    Identify ticket states by mapping them according to<br />
    settings/statusmap.properties</p>"""

  def fillPanel(panel: CustomizedGridBagPanel) {}
}