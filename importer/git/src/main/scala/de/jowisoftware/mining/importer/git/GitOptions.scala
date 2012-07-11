package de.jowisoftware.mining.importer.git

import de.jowisoftware.mining.UserOptions
import scala.swing.GridPanel
import scala.swing.Panel

class GitOptions extends UserOptions {
  protected var result = Map(("gitdir" -> "c:/temp/testrepos/.git"),
    ("repositoryname" -> "default"))

  val getHtmlDescription = """<p><b>Git Importer</b><br>
    Import a local git repository</p>"""

  def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Git dir", text("gitdir"))
    panel.add("Repository name", text("repositoryname"))
  }
}