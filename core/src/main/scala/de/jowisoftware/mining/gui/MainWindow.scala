package de.jowisoftware.mining.gui

import java.awt.Dimension
import scala.swing.event.{ WindowClosing, Event }
import scala.swing.{ TabbedPane, Frame, Alignment }
import MainWindow.DatabaseUpdated
import de.jowisoftware.mining.gui.importer.ImportPane
import de.jowisoftware.mining.gui.shell.ShellPane
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.plugins.PluginManager
import de.jowisoftware.neo4j.Database
import scala.swing.SplitPane
import de.jowisoftware.mining.gui.linker.LinkPane

object MainWindow {
  case object DatabaseUpdated extends Event
}

class MainWindow(db: Database[RootNode], pluginManager: PluginManager) extends Frame {
  import MainWindow._
  private val deletePane = new TabbedPane.Page("0) Delete", new DeletePane(db, this))
  private val importPane = new TabbedPane.Page("1) Import", new ImportPane(db, pluginManager, this))
  private val linkPane = new TabbedPane.Page("2) Link data", new LinkPane(db, pluginManager, this))
  private val shellPane = new TabbedPane.Page("3) Shell", new ShellPane(db.service))

  private val tabs = new TabbedPane {
    tabPlacement(Alignment.Left)

    pages += deletePane
    pages += importPane
    pages += linkPane
    pages += shellPane
  }

  title = "Ticketanalyzer"
  contents = tabs
  size = new Dimension(640, 480)
  importPane.content.asInstanceOf[SplitPane].dividerLocation = .75

  updateView()
  centerOnScreen()

  reactions += {
    case WindowClosing(_) => dispose()
    case DatabaseUpdated => updateView()
  }

  override def dispose() = {
    super.dispose

    db.shutdown
    scala.actors.Scheduler.shutdown()
    System.exit(0)
  }

  def updateView() {
    val state = db.inTransaction(t => t.rootNode.state())

    linkPane.enabled = state > 0
    shellPane.enabled = state > 0
    tabs.selection.index = state + 1
  }
}
