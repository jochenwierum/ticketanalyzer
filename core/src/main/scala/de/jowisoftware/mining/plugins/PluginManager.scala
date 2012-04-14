package de.jowisoftware.mining.plugins

class PluginManager {
  private var plugins: List[Plugin] = List()

  def addPlugin(plugin: Plugin) {
    plugins = plugin :: plugins
    println("Added: "+ plugin)
  }
}