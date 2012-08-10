package de.jowisoftware.mining.importer.git

import de.jowisoftware.mining.UserOptions
import scala.swing.GridPanel
import scala.swing.Panel

class GitOptions extends UserOptions("importer.git") {
  protected val defaultResult = Map(
    "gitdir" -> "/tmp/mygit/.git",
    "repositoryname" -> "default")

  protected val htmlDescription = """<p><b>Git Importer</b><br>
    Import a local git repository</p>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Git dir", text("gitdir"))
    panel.add("Repository name", text("repositoryname"))
  }
}