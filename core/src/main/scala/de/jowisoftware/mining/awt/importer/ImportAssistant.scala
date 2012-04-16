package de.jowisoftware.mining.awt.importer

import de.jowisoftware.mining.plugins.PluginManager
import de.jowisoftware.mining.awt.Assistant
import de.jowisoftware.mining.plugins.Plugin
import de.jowisoftware.mining.importer.Importer

class ImportAssistant {
  def show(manager: PluginManager) = {
    val ticketsPage, scmPage = new PluginSettingsWrapper()
    val selection = new PluginSelectionPage(manager, scmPage, ticketsPage)
    val assistant = new Assistant("Import", selection, scmPage, ticketsPage)

    val success = assistant.run
    var result: List[(Importer, Map[String, String])] = Nil

    if (success) {
      result = addPluginIfSelected(selection.scmPlugin, scmPage.result, result)
      result = addPluginIfSelected(selection.ticketsPlugin, ticketsPage.result, result)
    }

    result
  }

  private def addPluginIfSelected(plugin: Option[Plugin], config: Map[String, String],
    oldResult: List[(Importer, Map[String, String])]) = plugin match {
    case None => oldResult
    case Some(plugin) => (plugin.clazz.newInstance, config) :: oldResult
  }
}