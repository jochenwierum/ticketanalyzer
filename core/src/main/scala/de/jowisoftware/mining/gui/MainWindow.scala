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
import de.jowisoftware.mining.gui.linker.AnalyzerPane
import de.jowisoftware.mining.gui.shell.StatisticsPane

object MainWindow {
  case object DatabaseUpdated extends Event
}

class MainWindow(db: Database[RootNode], pluginManager: PluginManager) extends Frame {
  import MainWindow._
  private val deletePane = new TabbedPane.Page("0) Delete", new DeletePane(db, this))
  private val importPane = new TabbedPane.Page("1) Import", new ImportPane(db, pluginManager, this))
  private val linkPane = new TabbedPane.Page("2) Link data", new LinkPane(db, pluginManager, this))
  private val analyzePane = new TabbedPane.Page("3) Analyze", new AnalyzerPane(db, pluginManager, this))
  private val statisticsPane = new TabbedPane.Page("4) Statistics", new StatisticsPane(db, this))
  private val shellPane = new TabbedPane.Page("5) Shell", new ShellPane(db, this))

  private val tabs = new TabbedPane {
    tabPlacement = Alignment.Left

    pages += deletePane
    pages += importPane
    pages += linkPane
    pages += analyzePane
    pages += statisticsPane
    pages += shellPane
  }

  title = "Ticketanalyzer"
  contents = tabs
  size = new Dimension(640, 480)
  minimumSize = new Dimension(640, 480)

  updateView()
  centerOnScreen()
  for (tab <- tabs.pages.map(_.content.asInstanceOf[GuiTab])) {
    tab.align
  }

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
    val state = db.rootNode.state()

    linkPane.enabled = state > 0
    analyzePane.enabled = state > 1
    tabs.selection.index = state + 1

    shellPane.enabled = state > 0

    shellPane.content.asInstanceOf[ShellPane].newViewState(state)
    statisticsPane.content.asInstanceOf[StatisticsPane].updateStatistics()
  }
}
