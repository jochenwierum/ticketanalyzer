package de.jowisoftware.mining.awt.importer

import scala.swing.{ Panel, Label, BorderPanel }

import de.jowisoftware.mining.awt.AssistantPage
import de.jowisoftware.mining.importer.{ Importer, ImporterOptions }
import de.jowisoftware.mining.plugins.Plugin

class PluginSettingsWrapper extends AssistantPage {
  object dummyPage extends ImporterOptions {
    def getPanel = new BorderPanel() {
      layout(new Label("No plugin selected")) = BorderPanel.Position.Center
    }
    def getUserInput = Map()
  }

  private var _options: ImporterOptions = _
  private var _plugin: Option[Plugin] = _
  private var _title: String = _

  def plugin_=(plugin: Option[Plugin]) {
    _plugin = plugin
    _options = plugin match {
      case Some(plugin) => plugin.clazz.newInstance.showOptions
      case None => dummyPage
    }
  }
  def plugin = _plugin

  def result = _options.getUserInput

  def title: String = _plugin match {
    case Some(plugin) => plugin.name
    case None => ""
  }

  def getPanel: Panel = _options.getPanel
}