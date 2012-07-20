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

class LinkPane(db: Database[RootNode], pluginManager: PluginManager, parent: Frame) extends BorderPanel with GuiTab {
  private val pluginList = new ComboBox[Plugin](makePluginList)
  private var scmList = makeSCMList
  private var ticketList = makeTicketList
  private val linkButton = new Button("Link")
  private val pluginDetails = new ScrollPane

  private var selectedPlugin: Linker = _
  private var importerOptions: UserOptions = _

  private val selectionPanel = new GridPanel(3, 2) {
    contents += new LeftAlignedLabel("Plugin:")
    contents += pluginList
    contents += new LeftAlignedLabel("Ticket Repository:")
    contents += ticketList
    contents += new LeftAlignedLabel("Source Repository:")
    contents += scmList
  }

  layout += selectionPanel -> Position.North
  layout += pluginDetails -> Position.Center
  layout += linkButton -> Position.South

  listenTo(parent)
  listenTo(linkButton)
  listenTo(pluginList.selection)

  reactions += {
    case DatabaseUpdated => updateComboBoxes
    case SelectionChanged(`pluginList`) => updateSelection()
    case ButtonClicked(`linkButton`) => doLink()
  }

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

  private def doLink() {
    val dialog = new ProgressDialog(parent)
    new Thread("linker-thread") {
      override def run() {
        val options = importerOptions.getUserInput

        try {
          selectedPlugin.link(getSelectedTicketRepository,
            getSelectedCommitRepository, options,
            new DatabaseLinkerHandler(db, ticketList.selection.item, scmList.selection.item) with ConsoleProgressReporter with LinkerEventGui {
              val progressDialog = dialog
            })

          if (db.rootNode.state() < 2) {
            db.inTransaction { transaction =>
              transaction.rootNode.state(2)
              println(transaction.rootNode.state())
              transaction.success()
            }
          }

        } finally {
          Swing.onEDT {
            dialog.hide()
            parent.publish(DatabaseUpdated)
          }
        }
      }
    }.start()
    dialog.show()
  }

  private def getSelectedTicketRepository =
    db.rootNode.ticketRepositoryCollection.findOrCreateChild(ticketList.selection.item)

  private def getSelectedCommitRepository =
    db.rootNode.commitRepositoryCollection.findOrCreateChild(scmList.selection.item)

  def align = {}
}