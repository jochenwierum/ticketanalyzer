package de.jowisoftware.mining.linker.statustype

import de.jowisoftware.mining.UserOptions

class StatusTypeOptions extends UserOptions("linker.trac") {
  protected val defaultResult: Map[String, String] = Map()

  protected val htmlDescription = """<p><b>Link Ticket Types</b><br />
    Identify ticket states by mapping them according to<br />
    settings/linker-type-statusmap.properties</p>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) {}
}