package de.jowisoftware.mining.gui

import scala.swing.Frame
import scala.swing.event.WindowClosing
import scala.swing.TabbedPane
import scala.swing.Alignment
import de.jowisoftware.mining.plugins.PluginManager
import de.jowisoftware.mining.gui.importer.ImportPane
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.model.RootNode
import scala.swing.event.Event
import de.jowisoftware.mining.gui.linker.LinkPane
import de.jowisoftware.mining.gui.linker.LinkPane
import de.jowisoftware.mining.gui.shell.ShellPane

object MainWindow {
  case object DatabaseUpdated extends Event
}

class MainWindow(db: Database[RootNode], pluginManager: PluginManager) extends Frame { frame =>
  import MainWindow._
  private val importPane = new TabbedPane.Page("1) Import", new ImportPane(db, pluginManager, frame))
  private val linkPane = new TabbedPane.Page("2) Link data", new LinkPane(db, pluginManager, frame))
  private val shellPane = new TabbedPane.Page("3) Shell", new ShellPane(db.service))

  private val tabs = new TabbedPane {
    tabPlacement(Alignment.Left)

    pages += importPane
    pages += linkPane
    pages += shellPane
  }


  title = "Ticketanalyzer"

  contents = tabs
  updateView()
  pack()
  centerOnScreen()

  reactions += {
    case WindowClosing(_) => dispose()
    case DatabaseUpdated => updateView()
  }

  override def dispose() = {
    super.dispose

    db.shutdown
    scala.actors.Scheduler.shutdown()
  }

  def updateView() {
    val state = db.inTransaction(t => t.rootNode.state())

    linkPane.enabled = state == 1
    shellPane.enabled = state > 0
    tabs.selection.index = state
  }
}