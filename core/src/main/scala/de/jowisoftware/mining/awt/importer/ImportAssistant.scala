package de.jowisoftware.mining.awt.importer

import de.jowisoftware.mining.plugins.PluginManager
import de.jowisoftware.mining.awt.Assistant

class ImportAssistant {
  def run(manager: PluginManager) {
    val ticketsPage, scmPage = new PluginSettingsWrapper()
    val selection = new PluginSelectionPage(manager, scmPage, ticketsPage)
    val assistant = new Assistant("Import", selection, scmPage, ticketsPage)
    assistant.visible = true
  }
}