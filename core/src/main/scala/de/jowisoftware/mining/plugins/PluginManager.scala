package de.jowisoftware.mining.plugins

class PluginManager {
  private var plugins: List[Plugin] = List()

  def addPlugin(plugin: Plugin) {
    plugins = plugin :: plugins
  }

  def getFor(pluginType: PluginType.Value) =
    plugins.filter(_.pluginType == pluginType)
}