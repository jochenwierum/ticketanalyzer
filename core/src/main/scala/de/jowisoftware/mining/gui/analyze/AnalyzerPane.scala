package de.jowisoftware.mining.gui.linker

import scala.swing.{ BoxPanel, Button, ComboBox, Dialog, Frame, Orientation, ScrollPane, Swing }
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position
import scala.swing.event.{ ButtonClicked, SelectionChanged }
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.analyzer.Analyzer
import de.jowisoftware.mining.gui.{ GuiTab, ProgressDialog }
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.plugins.{ Plugin, PluginManager, PluginType }
import de.jowisoftware.neo4j.Database
import grizzled.slf4j.Logging
import javax.swing.JLabel
import scala.swing.Label
import de.jowisoftware.mining.gui.results.ResultWindow

class AnalyzerPane(db: Database, pluginManager: PluginManager, parent: Frame)
    extends BorderPanel with GuiTab with Logging { that =>
  private val pluginList = new ComboBox(makePluginList)
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
    try {
      val plugin = pluginList.selection.item
      selectedPlugin = plugin.clazz.newInstance.asInstanceOf[Analyzer]
      analyzerOptions = selectedPlugin.userOptions

      pluginDetails.contents = new BoxPanel(Orientation.Vertical) {
        contents += analyzerOptions.getPanel
        contents += Swing.VGlue
      }
      analyzeButton.enabled = true
    } catch {
      case e: Exception =>
        pluginDetails.contents = new Label("<html><strong>"+e.getClass.getName+
          "</strong>:<br />"+e.getMessage()+"</html>")
        analyzeButton.enabled = false
    }
    pluginDetails.revalidate()
  }

  def analyze() {
    val dialog = new ProgressDialog(parent)
    new Thread("analyzer-thread") {
      override def run() {
        db.inTransaction { transaction =>
          val options = analyzerOptions.getUserInput
          val resultOption = try {
            Some(selectedPlugin.analyze(transaction, options, dialog))
          } catch {
            case e: Exception =>
              error("Caught exception while running analyzer "+selectedPlugin.getClass.getName, e)
              Dialog.showMessage(that, "Error in analyzer: "+e.getClass.getName+": "+
                e.getMessage, "Error", Dialog.Message.Error)
              None
          } finally {
            dialog.hide()
          }

          for (result <- resultOption) {
            Swing.onEDTWait {
              val resultWindow = new ResultWindow(parent, result)
              resultWindow.visible = true
            }
          }
        }
      }
    }.start()
    dialog.show()
  }

  def align = {}
}