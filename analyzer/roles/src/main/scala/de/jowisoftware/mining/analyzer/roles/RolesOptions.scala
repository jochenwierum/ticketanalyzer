package de.jowisoftware.mining.analyzer.roles

import de.jowisoftware.mining.UserOptions

class RolesOptions extends UserOptions {
  protected val htmlDescription = """<b></b>"""
  protected val defaultResult: Map[String, String] = Map()
  protected def fillPanel(panel: CustomizedGridBagPanel) = {}
}