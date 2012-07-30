package de.jowisoftware.mining.plugins
import de.jowisoftware.mining.importer.Importer

case class Plugin(pluginType: PluginType.Value, name: String, clazz: Class[_]) {
  override def toString = name
}