package de.jowisoftware.mining.gui.importer

import scala.swing.{ ScrollPane, SplitPane, ListView, BorderPanel, Button, ComboBox, GridPanel, Frame, Orientation }
import scala.swing.event.{ ButtonClicked, SelectionChanged }
import scala.swing.ScrollPane.BarPolicy
import scala.swing.BorderPanel.Position
import de.jowisoftware.mining.gui.MainWindow.DatabaseUpdated
import de.jowisoftware.mining.gui.{ ProgressDialog }
import de.jowisoftware.mining.importer.async.{ AsyncDatabaseImportHandler, ConsoleProgressReporter }
import de.jowisoftware.mining.importer.Importer
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.plugins.{ Plugin, PluginType, PluginManager }
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.neo4j.{ Database, DBWithTransaction }
import scala.swing.BoxPanel
import javax.swing.BoxLayout
import javax.swing.Box
import scala.swing.Swing
import grizzled.slf4j.Logging
import de.jowisoftware.mining.gui.GuiTab
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.mining.model.nodes.helper.MiningNode

class ImportPane(
    db: Database[RootNode],
    pluginManager: PluginManager,
    parent: Frame) extends SplitPane(Orientation.Vertical) with GuiTab with Logging { self =>

  private class Task(val importer: Importer, val data: Map[String, String], val name: String) {
    override def toString =
      name + (if (data.contains("repositoryname")) " ["+data("repositoryname")+"]" else "")
  }

  private var selectedPlugin: Importer = _
  private var importerOptions: UserOptions = _
  private var tasks: List[Task] = Nil

  private val importButton = new Button("Import")
  private val deleteButton = new Button("Delete")
  private val addButton = new Button("Add")
  private val pluginList = new ComboBox(makePluginList)
  private val pluginDetails = new ScrollPane
  private val taskList = new ListView[Task]

  private val buttons = new GridPanel(2, 1) {
    contents += deleteButton
    contents += importButton
  }

  leftComponent = new BorderPanel() {
    layout += pluginList -> Position.North
    layout += pluginDetails -> Position.Center
    layout += addButton -> Position.South
  }

  rightComponent = new BorderPanel() {
    layout += taskList -> Position.Center
    layout += buttons -> Position.South
  }

  listenTo(deleteButton)
  listenTo(importButton)
  listenTo(addButton)
  listenTo(pluginList.selection)

  reactions += {
    case ButtonClicked(`importButton`) => runImport()
    case ButtonClicked(`addButton`) => queueTask()
    case ButtonClicked(`deleteButton`) => deleteTask()
    case SelectionChanged(`pluginList`) => updateSelection()
  }

  updateTaskList()
  updateSelection()

  def makePluginList =
    pluginManager.getFor(PluginType.SCM) ++ pluginManager.getFor(PluginType.ITS)

  def queueTask() {
    tasks = new Task(selectedPlugin, importerOptions.getUserInput,
      pluginList.selection.item.toString) :: tasks
    updateTaskList()
  }

  def deleteTask() {
    val toDelete = taskList.selection.items
    tasks = tasks.filterNot(item => toDelete contains item)
    updateTaskList()
  }

  def updateTaskList() {
    taskList.listData = tasks
    deleteButton.enabled = tasks.length > 0
    importButton.enabled = tasks.length > 0
  }

  def updateSelection() {
    val plugin = pluginList.selection.item
    selectedPlugin = plugin.clazz.newInstance.asInstanceOf[Importer]
    importerOptions = selectedPlugin.userOptions

    pluginDetails.contents = new BoxPanel(Orientation.Vertical) {
      contents += importerOptions.getPanel
      contents += Swing.VGlue
    }
    pluginDetails.revalidate()
  }

  def runImport() {
    val progress = new ProgressDialog(parent)

    new Thread("importer-thread") {
      override def run = {
        val start = System.currentTimeMillis
        try {
          val importer = new AsyncDatabaseImportHandler(
            db,
            tasks.map { t => (t.importer, t.data) }.toArray: _*) with ConsoleProgressReporter with ImporterEventGui {
            var dialog = progress
          }
          importer.run()

          warn("Import process took "+(System.currentTimeMillis - start)+" ms")
          updateDBState
          tasks = Nil
        } finally {
          Swing.onEDT {
            updateTaskList()
            progress.hide()
            parent.publish(DatabaseUpdated)
          }
        }
      }
    }.start()
    progress.show()
  }

  private def updateDBState() = {
    db.inTransaction { transaction: DBWithTransaction[RootNode] =>
      def hasContent(repository: MiningNode) =
        repository.neighbors(Direction.OUTGOING, Seq(Contains.relationType)).size > 0

      val root = transaction.rootNode
      val hasCommits = hasContent(root.commitRepositoryCollection)
      val hasTickets = hasContent(root.ticketRepositoryCollection)
      if (hasCommits && hasTickets && root.state() < 1) {
        root.state(1)
      }

      transaction.success
    }
  }

  def align = dividerLocation = .75
}
