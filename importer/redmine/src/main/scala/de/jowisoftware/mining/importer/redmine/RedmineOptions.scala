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

  def getPanel(): Panel = new GridPanel(4, 2) {
    contents += label("Url")
    contents += text("url")

    contents += label("API-Key")
    contents += text("key")

    contents += label("Project id")
    contents += text("project")

    contents += label("Repository name")
    contents += text("repositoryname")
  }
}