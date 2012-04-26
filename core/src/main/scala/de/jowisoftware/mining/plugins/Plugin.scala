package de.jowisoftware.mining.plugins
import de.jowisoftware.mining.importer.Importer

case class Plugin(pluginType: PluginType.PluginType, name: String, clazz: Class[Importer]) {
  override def toString = name+" ("+pluginType+")"
}