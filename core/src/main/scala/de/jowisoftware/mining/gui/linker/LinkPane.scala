package de.jowisoftware.mining.gui.linker

import scala.swing.BorderPanel.Position
import scala.swing.event.{ SelectionChanged, ButtonClicked }
import scala.swing.{ Swing, ScrollPane, Orientation, GridPanel, Frame, Button, BoxPanel, BorderPanel, ComboBox }

import org.neo4j.graphdb.Direction

import de.jowisoftware.mining.gui.MainWindow.DatabaseUpdated
import de.jowisoftware.mining.gui.{ ProgressDialog, LeftAlignedLabel }
import de.jowisoftware.mining.linker.Linker
import de.jowisoftware.mining.model.{ RootNode, Node, HasName, Contains }
import de.jowisoftware.mining.plugins.{ PluginType, PluginManager, Plugin }
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }

class LinkPane(db: Database[RootNode], pluginManager: PluginManager, parent: Frame) extends BorderPanel {
  private val pluginList = new ComboBox[Plugin](makePluginList)
  private var scmList = makeSCMList
  private var ticketList = makeTicketList
  private val linkButton = new Button("link")
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
  layout += linkButton -> Position.South

  listenTo(parent)
  listenTo(linkButton)
  listenTo(pluginList)

  reactions += {
    case DatabaseUpdated => updateComboBoxes
    case SelectionChanged(`pluginList`) => updateSelection()
    case ButtonClicked(`linkButton`) => doLink
  }

  updateSelection

  def makePluginList =
    pluginManager.getFor(PluginType.Linker)

  def makeSCMList = new ComboBox[String](db.inTransaction { transaction =>
    namesOfChildren(transaction.rootNode.commitRepositoryCollection)
  }.toSeq)

  def makeTicketList = new ComboBox[String](db.inTransaction { transaction =>
    namesOfChildren(transaction.rootNode.ticketRepositoryCollection)
  }.toSeq)

  def namesOfChildren(repository: Node) = {
    val nodes = repository.neighbors(Direction.OUTGOING, Seq(Contains.relationType))
    nodes.map { node => node.asInstanceOf[HasName].name() }
  }

  def updateComboBoxes() {
    scmList = makeSCMList
    selectionPanel.contents(3) = scmList
    ticketList = makeTicketList
    selectionPanel.contents(5) = ticketList
  }

  def updateSelection() {
    val plugin = pluginList.selection.item
    selectedPlugin = plugin.clazz.newInstance.asInstanceOf[Linker]
    importerOptions = selectedPlugin.userOptions

    pluginDetails.contents = new BoxPanel(Orientation.Vertical) {
      contents += importerOptions.getPanel
      contents += Swing.VGlue
    }
    pluginDetails.revalidate()
  }

  def doLink() {
    val progress = new ProgressDialog(parent)
    new Thread("linker-thread") {
      override def run() {
        val options = importerOptions.getUserInput

        db.inTransaction { transaction =>
          selectedPlugin.link(getSelectedTicketRepository(transaction),
            getSelectedCommitRepository(transaction), options, new LinkerEventGui(progress))

          if (transaction.rootNode.state() < 2) {
            transaction.rootNode.state(2)
          }

          transaction.success

          Swing.onEDT {
            progress.hide()
            parent.publish(DatabaseUpdated)
          }
        }
      }
    }.start()
    progress.show()
  }

  def getSelectedTicketRepository(transaction: DBWithTransaction[RootNode]) =
    transaction.rootNode.ticketRepositoryCollection.findOrCreateChild(ticketList.selection.item)

  def getSelectedCommitRepository(transaction: DBWithTransaction[RootNode]) =
    transaction.rootNode.commitRepositoryCollection.findOrCreateChild(scmList.selection.item)
}