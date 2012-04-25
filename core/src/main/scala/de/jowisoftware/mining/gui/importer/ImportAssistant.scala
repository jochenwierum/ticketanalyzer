package de.jowisoftware.mining.gui.importer

import de.jowisoftware.mining.plugins.PluginManager
import de.jowisoftware.mining.gui.Assistant
import de.jowisoftware.mining.plugins.Plugin
import de.jowisoftware.mining.importer.Importer
import scala.swing.Frame

class ImportAssistant(manager: PluginManager) {
  def show(owner: Frame) = {
    val ticketsPage, scmPage = new PluginSettingsWrapper()
    val selection = new PluginSelectionPage(manager, scmPage, ticketsPage)
    val assistant = new Assistant("Import", owner, selection, scmPage, ticketsPage)

    val success = assistant.run
    if (success) {
      var result: List[(Importer, Map[String, String])] = Nil
      result = addPluginIfSelected(selection.scmPlugin, scmPage.result, result)
      result = addPluginIfSelected(selection.ticketsPlugin, ticketsPage.result, result)
      Some(result)
    } else {
      None
    }
  }

  private def addPluginIfSelected(plugin: Option[Plugin], config: Map[String, String],
    oldResult: List[(Importer, Map[String, String])]) = plugin match {
    case None => oldResult
    case Some(plugin) => (plugin.clazz.newInstance, config) :: oldResult
  }
}