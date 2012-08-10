package de.jowisoftware.mining.importer.redmine

import de.jowisoftware.mining.UserOptions
import scala.swing.Panel
import scala.swing.GridPanel

class RedmineOptions extends UserOptions("Redmine") {
  protected val defaultResult = Map(
    "url" -> "http://example.org/redmine",
    "key" -> "",
    "project" -> "1",
    "repositoryname" -> "default")

  protected val htmlDescription = "<b>Redmine Importer</b>"

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Url", text("url"))
    panel.add("API-Key", text("key"))
    panel.add("Project id", text("project"))
    panel.add("Repository name", text("repositoryname"))
  }
}