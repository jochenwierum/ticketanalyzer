package de.jowisoftware.mining.gui.linker

import scala.swing.BorderPanel.Position
import scala.swing.event.{ SelectionChanged, ButtonClicked }
import scala.swing.{ Swing, ScrollPane, Orientation, GridPanel, Frame, Button, BoxPanel, BorderPanel, ComboBox }
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.gui.MainWindow.DatabaseUpdated
import de.jowisoftware.mining.gui.{ ProgressDialog, LeftAlignedLabel }
import de.jowisoftware.mining.linker.Linker
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.mining.model.nodes.helper.{ MiningNode, HasName }
import de.jowisoftware.mining.plugins.{ PluginType, PluginManager, Plugin }
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }
import de.jowisoftware.mining.linker.ConsoleProgressReporter
import de.jowisoftware.mining.linker.DatabaseLinkerHandler
import de.jowisoftware.mining.gui.GuiTab
import de.jowisoftware.mining.analyzer.Analyzer

class AnalyzerPane(db: Database[RootNode], pluginManager: PluginManager, parent: Frame)
    extends BorderPanel with GuiTab {
  private val pluginList = new ComboBox[Plugin](makePluginList)
  private val analyzeButton = new Button("Analyze")
  private val pluginDetails = new ScrollPane

  private var selectedPlugin: Analyzer = _
  private var analyzerOptions: UserOptions = _

  layout += pluginList -> Position.North
  layout += pluginDetails -> Position.Center
  layout += analyzeButton -> Position.South

  listenTo(parent)
  listenTo(analyzeButton)
  listenTo(pluginList.selection)

  reactions += {
    case SelectionChanged(`pluginList`) => updateSelection()
    case ButtonClicked(`analyzeButton`) => analyze()
  }

  updateSelection

  private def makePluginList =
    pluginManager.getFor(PluginType.Analyzer)

  def updateSelection() {
    val plugin = pluginList.selection.item
    selectedPlugin = plugin.clazz.newInstance.asInstanceOf[Analyzer]
    analyzerOptions = selectedPlugin.userOptions

    pluginDetails.contents = new BoxPanel(Orientation.Vertical) {
      contents += analyzerOptions.getPanel
      contents += Swing.VGlue
    }
    pluginDetails.revalidate()
  }

  def analyze() {
    val dialog = new ProgressDialog(parent)
    new Thread("analyzer-thread") {
      override def run() {
        val options = analyzerOptions.getUserInput
        try {
          selectedPlugin.analyze(db, options, parent, dialog)
        } finally {
          dialog.hide()
        }
      }
    }.start()
    dialog.show()
  }

  def align = {}
}