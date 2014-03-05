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
import scala.swing.Dialog
import scala.swing.Label
import de.jowisoftware.mining.model.nodes.CommitRepository
import org.neo4j.graphdb.ResourceIterable
import de.jowisoftware.util.ScalaUtil._
import scala.collection.JavaConversions._
import de.jowisoftware.mining.model.nodes.TicketRepository

class LinkPane(db: Database, pluginManager: PluginManager, parent: Frame)
    extends SplitPane(Orientation.Vertical) with GuiTab with Logging { that =>

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

  private def makeSCMList = new ComboBox[String](
    db.inTransaction(t => collectNames(CommitRepository.findAll(t))))

  private def makeTicketList = new ComboBox[String](
    db.inTransaction(t => collectNames(TicketRepository.findAll(t))))

  private def collectNames(result: Seq[_ <: HasName]) = result.map(_.name())

  private def updateComboBoxes() {
    scmList = makeSCMList
    selectionPanel.contents(3) = scmList
    ticketList = makeTicketList
    selectionPanel.contents(5) = ticketList
  }

  private def updateSelection() {
    try {
      val plugin = pluginList.selection.item
      selectedPlugin = plugin.clazz.newInstance.asInstanceOf[Linker]
      importerOptions = selectedPlugin.userOptions

      pluginDetails.contents = new BoxPanel(Orientation.Vertical) {
        contents += importerOptions.getPanel
        contents += Swing.VGlue
      }
      linkButton.enabled = true
    } catch {
      case e: Exception =>
        pluginDetails.contents = new Label("<html><strong>"+e.getClass.getName+
          "</strong>:<br />"+e.getMessage()+"</html>")
        linkButton.enabled = false
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
    new Thread("linker-thread") {
      override def run() {
        val start = System.currentTimeMillis
        try {
          tasks.foreach(runTask(_, dialog))
          warn("Linking process took "+(System.currentTimeMillis - start)+" ms")
          updateDBState
          tasks = Nil
        } catch {
          case e: Exception =>
            error("Caught exception while running linker "+selectedPlugin.getClass.getName, e)
            Dialog.showMessage(that, "Error in linker: "+e.getMessage, "Error", Dialog.Message.Error)
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
    task.importer.link(db, getSelectedTicketRepository,
      getSelectedCommitRepository, options,
      new DatabaseLinkerHandler(db, ticketList.selection.item, scmList.selection.item) with ConsoleProgressReporter with LinkerEventGui {

        val progressDialog = dialog
      })
  }

  private def updateDBState: Unit = {
    db.inTransaction { transaction =>
      val rootNode = transaction.rootNode(RootNode)
      if (rootNode.state() < 2) {
        rootNode.state(2)
        transaction.success()
      }
    }
  }

  private def getSelectedTicketRepository =
    db.inTransaction(t => TicketRepository.find(t, ticketList.selection.item).get)

  private def getSelectedCommitRepository =
    db.inTransaction(t => CommitRepository.find(t, ticketList.selection.item).get)

  def align = dividerLocation = .75
}