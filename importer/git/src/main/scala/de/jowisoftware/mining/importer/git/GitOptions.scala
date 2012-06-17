package de.jowisoftware.mining.importer.git

import de.jowisoftware.mining.UserOptions
import scala.swing.GridPanel
import scala.swing.Panel

class GitOptions extends UserOptions {
  protected var result = Map(("gitdir" -> "c:/temp/testrepos/.git"),
    ("repositoryname" -> "default"))

  def getPanel(): Panel = new GridPanel(2, 2) {
    contents += label("Git dir")
    contents += text("gitdir")

    contents += label("Repository name")
    contents += text("repositoryname")
  }
}