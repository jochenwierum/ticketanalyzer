package de.jowisoftware.mining.plugins

import grizzled.slf4j.Logging
import scala.collection.SortedSet

class PluginManager extends Logging {
  private var plugins = SortedSet.empty[Plugin](Ordering.by(_.toString))

  def addPlugin(plugin: Plugin) {
    warn("Found plugin: "+plugin)
    plugins += plugin
  }

  def getFor(pluginType: PluginType.Value) =
    plugins.filter(_.pluginType == pluginType).toList
}