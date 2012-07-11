package de.jowisoftware.mining.importer.redmine

import de.jowisoftware.mining.UserOptions
import scala.swing.Panel
import scala.swing.GridPanel

class RedmineOptions extends UserOptions {
  protected var result = Map(
    "url" -> "http://jowisoftware.de:3000/",
    "key" -> "2ae8befe0e72f5cc5c3f0e8f364fe1c34ee340b5",
    "project" -> "1",
    "repositoryname" -> "default")

  val getHtmlDescription = "<b>Redmine Importer</b>"

  def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Url", text("url"))
    panel.add("API-Key", text("key"))
    panel.add("Project id", text("project"))
    panel.add("Repository name", text("repositoryname"))
  }
}