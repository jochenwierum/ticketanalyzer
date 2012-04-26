package de.jowisoftware.mining.gui.importer

import scala.swing.{ SplitPane, ListView, BorderPanel, Button, ComboBox, GridPanel, Frame, Orientation }
import scala.swing.event.{ ButtonClicked, SelectionChanged }
import scala.swing.BorderPanel.Position
import scala.swing.ListView.AbstractRenderer

import de.jowisoftware.neo4j.{ Database, DBWithTransaction }
import de.jowisoftware.mining.plugins.{ Plugin, PluginType, PluginManager }
import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.mining.gui.MainWindow.DatabaseUpdated
import de.jowisoftware.mining.importer.async.{ AsyncDatabaseImportHandler, ConsoleProgressReporter }
import de.jowisoftware.mining.importer.{ Importer, ImporterOptions }

class ImportPane(
    db: Database[RootNode],
    pluginManager: PluginManager,
    parent: Frame) extends SplitPane(Orientation.Vertical) {

  private class Task(val importer: Importer, val data: Map[String, String], val name: String) {
    override def toString =
      name + (if (data.contains("repositoryname")) " ["+data("repositoryname")+"]" else "")
  }

  private val importButton = new Button("Import")
  private val deleteButton = new Button("Delete")
  private val addButton = new Button("Add")
  private val pluginList = new ComboBox(makePluginList)
  private val pluginDetails = new BorderPanel()
  private val taskList = new ListView[Task]

  private var selectedPlugin: Importer = _
  private var importerOptions: ImporterOptions = _
  private var tasks: List[Task] = Nil

  private val buttons = new GridPanel(2, 1) {
    contents += deleteButton
    contents += importButton
  }

  leftComponent = new BorderPanel() {
    layout(pluginList) = Position.North
    layout(pluginDetails) = Position.Center
    layout(addButton) = Position.South
  };

  rightComponent = new BorderPanel() {
    layout(taskList) = Position.Center
    layout(buttons) = Position.South
  };

  addButton.enabled = false
  dividerLocation = .75

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

  updateSelection()

  def makePluginList =
    pluginManager.getFor(PluginType.SCM) ++ pluginManager.getFor(PluginType.Tickets)

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
    selectedPlugin = plugin.clazz.newInstance
    importerOptions = selectedPlugin.showOptions

    pluginDetails.layout(importerOptions.getPanel) = Position.North
    pluginDetails.revalidate()

    addButton.enabled = true
  }

  def runImport() {
    db.inTransaction { trans: DBWithTransaction[RootNode] =>
      val importer = new AsyncDatabaseImportHandler(
        trans.rootNode, tasks.map { t => (t.importer, t.data) }.toArray: _*) with ConsoleProgressReporter
      importer.run()

      if (trans.rootNode.state() < 1) {
        trans.rootNode.state(1)
      }
      trans.success
    }

    parent.publish(DatabaseUpdated)
  }
}