package de.jowisoftware.mining.plugins

import java.net.{ URLClassLoader, URL }
import scala.Option.option2Iterable
import scala.collection.JavaConversions._
import de.jowisoftware.mining.importer.Importer
import grizzled.slf4j.Logging
import java.util.Properties
import java.io.File
import de.jowisoftware.util.FileUtils

class PluginScanner(basePath: File, pluginDirs: String*) extends Logging {
  def scan(manager: PluginManager) {
    info("Scanning in "+basePath+" for "+pluginDirs.mkString(", "))
    val jarFiles = pluginDirs.flatMap(FileUtils.expandPath(basePath, _)).map(_.getCanonicalFile)

    debug("Possible plugin files: "+jarFiles.mkString(", "))
    val classLoader = createClassLoader(jarFiles)
    getPluginInfos(classLoader).foreach(manager.addPlugin)
    info("Plugin scan finished")
  }

  private def createClassLoader(files: Seq[File]): ClassLoader =
    new URLClassLoader(files.map { _.toURI().toURL() }.toArray, getClass().getClassLoader())

  private def getPluginInfos(classLoader: ClassLoader) =
    mapToPlugins(classLoader, classLoader.getResources("META-INF/plugindata.properties"))

  private def mapToPlugins(classLoader: ClassLoader,
    resources: java.util.Enumeration[URL]): Iterator[Plugin] =
    resources.flatMap { url =>
      info("processing plugin information in "+url)
      val properties = new Properties
      properties.load(url.openStream)

      val pluginClass = properties.getProperty("class")
      val pluginType = properties.getProperty("type")
      val pluginName = properties.getProperty("name")

      try {
        if (pluginClass != null && pluginType != null && pluginName != null) {
          val clazz = classLoader.loadClass(pluginClass)
          Some(Plugin(PluginType.find(pluginType), pluginName, clazz.asInstanceOf[Class[Importer]]))
        } else {
          None
        }
      } catch {
        case e: Exception =>
          error("Error while inspecting plugin, skipping it", e)
          None
      }
    }
}
