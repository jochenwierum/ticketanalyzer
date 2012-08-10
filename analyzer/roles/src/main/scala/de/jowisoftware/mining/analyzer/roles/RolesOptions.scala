package de.jowisoftware.mining.analyzer.roles

import de.jowisoftware.mining.UserOptions

class RolesOptions extends UserOptions("analyzer.roles") {
  protected val htmlDescription = """<b>Identify project roles</b>"""
  protected val defaultResult: Map[String, String] = Map()
  protected def fillPanel(panel: CustomizedGridBagPanel) = {}
}