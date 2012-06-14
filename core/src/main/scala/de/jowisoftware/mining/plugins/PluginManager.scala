package de.jowisoftware.mining.plugins

import grizzled.slf4j.Logging

class PluginManager extends Logging {
  private var plugins: List[Plugin] = List()

  def addPlugin(plugin: Plugin) {
    warn("Found plugin: "+plugin)
    plugins = plugin :: plugins
  }

  def getFor(pluginType: PluginType.Value) =
    plugins.filter(_.pluginType == pluginType)
}