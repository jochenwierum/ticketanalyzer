package de.jowisoftware.mining.gui.linker

import scala.swing.{ Swing, ScrollPane, Orientation, GridPanel, Frame, ComboBox, Button, BoxPanel }
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position
import scala.swing.event.{ SelectionChanged, ButtonClicked }
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.gui.{ LeftAlignedLabel, GuiTab }
import de.jowisoftware.mining.gui.MainWindow.DatabaseUpdated
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.linker.{ Linker, DatabaseLinkerHandler, ConsoleProgressReporter }
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.model.nodes.helper.{ MiningNode, HasName }
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.mining.plugins.{ PluginType, PluginManager, Plugin }
import de.jowisoftware.neo4j.{ ReadOnlyDatabase, Database }
import scala.swing.ListView
import scala.swing.SplitPane
import grizzled.slf4j.Logging

class LinkPane(db: Database[RootNode], pluginManager: PluginManager, parent: Frame)
    extends SplitPane(Orientation.Vertical) with GuiTab with Logging {
  private class Task(val importer: Linker, val data: Map[String, String], val name: String) {
    override def toString = name
  }

  private var selectedPlugin: Linker = _
  private var importerOptions: UserOptions = _
  private var tasks: List[Task] = Nil

  private val pluginList = new ComboBox[Plugin](makePluginList)
  private var scmList = makeSCMList
  private var ticketList = makeTicketList
  private val deleteButton = new Button("Delete")
  private val addButton = new Button("Add")
  private val linkButton = new Button("Link")
  private val pluginDetails = new ScrollPane
  private val taskList = new ListView[Task]

  private val buttons = new GridPanel(2, 1) {
    contents += deleteButton
    contents += linkButton
  }

  private val selectionPanel = new GridPanel(3, 2) {
    contents += new LeftAlignedLabel("Plugin:")
    contents += pluginList
    contents += new LeftAlignedLabel("Ticket Repository:")
    contents += ticketList
    contents += new LeftAlignedLabel("Source Repository:")
    contents += scmList
  }

  leftComponent = new BorderPanel() {
    layout += selectionPanel -> Position.North
    layout += pluginDetails -> Position.Center
    layout += addButton -> Position.South
  }

  rightComponent = new BorderPanel() {
    layout += taskList -> Position.Center
    layout += buttons -> Position.South
  }

  listenTo(parent)
  listenTo(linkButton)
  listenTo(addButton)
  listenTo(deleteButton)
  listenTo(pluginList.selection)

  reactions += {
    case DatabaseUpdated => updateComboBoxes
    case SelectionChanged(`pluginList`) => updateSelection()
    case ButtonClicked(`linkButton`) => doLink()
    case ButtonClicked(`addButton`) => queueTask()
    case ButtonClicked(`deleteButton`) => deleteTask()
  }

  updateTaskList
  updateSelection

  private def makePluginList =
    pluginManager.getFor(PluginType.Linker)

  private def makeSCMList = new ComboBox[String](db.inTransaction { transaction =>
    val result = namesOfChildren(transaction.rootNode.commitRepositoryCollection)
    transaction.success
    result
  }.toSeq)

  private def makeTicketList = new ComboBox[String](db.inTransaction { transaction =>
    val result = namesOfChildren(transaction.rootNode.ticketRepositoryCollection)
    transaction.success
    result
  }.toSeq)

  private def namesOfChildren(repository: MiningNode) = {
    val nodes = repository.neighbors(Direction.OUTGOING, Seq(Contains.relationType))
    nodes.map { node => node.asInstanceOf[HasName].name() }
  }

  private def updateComboBoxes() {
    scmList = makeSCMList
    selectionPanel.contents(3) = scmList
    ticketList = makeTicketList
    selectionPanel.contents(5) = ticketList
  }

  private def updateSelection() {
    val plugin = pluginList.selection.item
    selectedPlugin = plugin.clazz.newInstance.asInstanceOf[Linker]
    importerOptions = selectedPlugin.userOptions

    pluginDetails.contents = new BoxPanel(Orientation.Vertical) {
      contents += importerOptions.getPanel
      contents += Swing.VGlue
    }
    pluginDetails.revalidate()
  }

  private def deleteTask() {
    val toDelete = taskList.selection.items
    tasks = tasks.filterNot(item => toDelete contains item)
    updateTaskList()
  }

  def queueTask() {
    tasks = new Task(selectedPlugin, importerOptions.getUserInput,
      pluginList.selection.item.toString) :: tasks
    updateTaskList()
  }

  def updateTaskList() {
    taskList.listData = tasks
    deleteButton.enabled = tasks.length > 0
    linkButton.enabled = tasks.length > 0
  }

  private def doLink() {
    val dialog = new ProgressDialog(parent)
    new Thread("linker-thread"){
      override def run() {
        val start = System.currentTimeMillis
        try {
          tasks.foreach(runTask(_, dialog))
          warn("Import process took "+(System.currentTimeMillis - start)+" ms")
          updateDBState
          tasks = Nil
        } finally {
          Swing.onEDT {
            updateTaskList()
            dialog.hide()
            parent.publish(DatabaseUpdated)
          }
        }
      }
    }.start()
    dialog.show()
  }

  private def runTask(task: Task, dialog: ProgressDialog): Unit = {
    val options = task.data
    task.importer.link(getSelectedTicketRepository,
      getSelectedCommitRepository, options,
      new DatabaseLinkerHandler(db, ticketList.selection.item, scmList.selection.item) with ConsoleProgressReporter with LinkerEventGui {

      val progressDialog = dialog
    })
  }

  private def updateDBState: Unit = {
    if (db.rootNode.state() < 2) {
      db.inTransaction { transaction =>
        transaction.rootNode.state(2)
        transaction.success()
      }
    }
  }

  private def getSelectedTicketRepository =
    db.rootNode.ticketRepositoryCollection.findOrCreateChild(ticketList.selection.item)

  private def getSelectedCommitRepository =
    db.rootNode.commitRepositoryCollection.findOrCreateChild(scmList.selection.item)

  def align = dividerLocation = .75
}