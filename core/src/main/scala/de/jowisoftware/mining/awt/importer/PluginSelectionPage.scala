package de.jowisoftware.mining.awt.importer

import de.jowisoftware.mining.plugins.Plugin
import de.jowisoftware.mining.plugins.PluginManager
import de.jowisoftware.mining.plugins.PluginType
import scala.swing.ListView.GenericRenderer
import scala.swing.ListView.Renderer
import scala.swing.event.ButtonClicked
import scala.swing.Button
import scala.swing.ComboBox
import scala.swing.Component
import scala.swing.Frame
import scala.swing.GridPanel
import scala.swing.Label
import de.jowisoftware.mining.awt.AssistantPage

class PluginSelectionPage(pluginManager: PluginManager,
    scmPage: PluginSettingsWrapper, ticketsPage: PluginSettingsWrapper) extends AssistantPage {
  val title = "Select Plugins"
  private val scmList = mkList(PluginType.SCM)
  private val ticketsList = mkList(PluginType.Tickets)

  override def save() = {
    scmPage.plugin = if (scmList.selection.index == 0) None else Some(scmList.selection.item)
    ticketsPage.plugin = if (ticketsList.selection.index == 0) None else Some(ticketsList.selection.item)
    true
  }

  def mkList(pluginType: PluginType.PluginType) = {
    val plugins = pluginManager.getFor(pluginType)
    val control = new ComboBox(Plugin(pluginType, "(none)", null) :: plugins)
    control.renderer = new Renderer[Plugin] {
      def componentFor(list: scala.swing.ListView[_], isSelected: Boolean, focused: Boolean, a: Plugin, index: Int): Component =
        GenericRenderer.componentFor(list, isSelected, focused, a.name, index)
    }
    control
  }

  def getPanel = new GridPanel(3, 2) {
    contents += new Label("SCM:")
    contents += scmList
    contents += new Label("Tickets:")
    contents += ticketsList
  }
}