package de.jowisoftware.mining.gui.importer

import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.gui.MainWindow.DatabaseUpdated
import de.jowisoftware.mining.gui.{GuiTab, ProgressDialog}
import de.jowisoftware.mining.importer.Importer
import de.jowisoftware.mining.importer.async.AsyncDatabaseImportHandlerWithFeedback
import de.jowisoftware.mining.model.nodes.{CommitRepository, RootNode, TicketRepository}
import de.jowisoftware.mining.plugins.{PluginManager, PluginType}
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.content.Node
import grizzled.slf4j.Logging

import scala.swing.BorderPanel.Position
import scala.swing.event.{ButtonClicked, SelectionChanged}
import scala.swing.{BorderPanel, BoxPanel, Button, ComboBox, Frame, GridPanel, ListView, Orientation, ScrollPane, SplitPane, Swing}

class ImportPane(
    db: Database,
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

  def queueTask(): Unit = {
    tasks = new Task(selectedPlugin, importerOptions.getUserInput,
      pluginList.selection.item.toString) :: tasks
    updateTaskList()
  }

  def deleteTask(): Unit = {
    val toDelete = taskList.selection.items
    tasks = tasks.filterNot(item => toDelete contains item)
    updateTaskList()
  }

  def updateTaskList(): Unit = {
    taskList.listData = tasks
    deleteButton.enabled = tasks.length > 0
    importButton.enabled = tasks.length > 0
  }

  def updateSelection(): Unit = {
    val plugin = pluginList.selection.item
    selectedPlugin = plugin.clazz.newInstance.asInstanceOf[Importer]
    importerOptions = selectedPlugin.userOptions

    pluginDetails.contents = new BoxPanel(Orientation.Vertical) {
      contents += importerOptions.getPanel
      contents += Swing.VGlue
    }
    pluginDetails.revalidate()
  }

  def runImport(): Unit = {
    val progress = new ProgressDialog(parent)

    new Thread("importer-thread") {
      override def run(): Unit = {
        val start = System.currentTimeMillis
        try {
          AsyncDatabaseImportHandlerWithFeedback.run(
            db,
            progress,
            tasks.map { t => (t.importer, t.data) }.toArray: _*)

          warn(s"Import process took ${System.currentTimeMillis - start} ms")
          updateDBState()
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
    db.inTransaction { transaction =>
      def hasContent(s: Seq[_ <: Node]): Boolean = s.nonEmpty

      val root = transaction.rootNode(RootNode)
      val hasCommits = hasContent(CommitRepository.findAll(transaction))
      val hasTickets = hasContent(TicketRepository.findAll(transaction))
      if (hasCommits && hasTickets && root.state() < 1) {
        root.state(1)
      }

      transaction.success()
    }
  }

  def align = dividerLocation = .75
}
