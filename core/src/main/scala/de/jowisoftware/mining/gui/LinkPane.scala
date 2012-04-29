package de.jowisoftware.mining.gui

import scala.swing.BorderPanel.Position
import scala.swing.{ Orientation, Frame, Button, BoxPanel, BorderPanel, ComboBox }
import org.neo4j.graphdb.Direction
import MainWindow.DatabaseUpdated
import de.jowisoftware.mining.model.{ RootNode, Node, HasName, Contains }
import de.jowisoftware.mining.plugins.{ PluginType, PluginManager, Plugin }
import de.jowisoftware.neo4j.Database
import scala.swing.GridPanel
import scala.swing.Alignment

class LinkPane(db: Database[RootNode], pluginManager: PluginManager, parent: Frame) extends BorderPanel {
  private val pluginList = new ComboBox[Plugin](makePluginList)
  private val scmList = new ComboBox[String](makeSCMList)
  private val ticketList = new ComboBox[String](makeTicketList)
  private val linkButton = new Button("link")

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

  reactions += {
    case DatabaseUpdated => updateComboBoxes
  }

  def makePluginList =
    pluginManager.getFor(PluginType.Linker)

  def makeSCMList = db.inTransaction { transaction =>
    namesOfChildren(transaction.rootNode.commitRepositoryCollection)
  }.toSeq

  def makeTicketList = db.inTransaction { transaction =>
    namesOfChildren(transaction.rootNode.ticketRepositoryCollection)
  }.toSeq

  def namesOfChildren(repository: Node) = {
    val nodes = repository.neighbors(Direction.OUTGOING, Seq(Contains.relationType))
    nodes.map { node => node.asInstanceOf[HasName].name() }
  }

  def updateComboBoxes = {
    scmList.peer.setModel(ComboBox.newConstantModel(makeSCMList))
    ticketList.peer.setModel(ComboBox.newConstantModel(makeTicketList))
  }
}