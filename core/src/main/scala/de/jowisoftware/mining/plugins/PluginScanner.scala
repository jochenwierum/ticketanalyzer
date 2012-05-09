package de.jowisoftware.mining.plugins
import java.io.File
import java.net.{ URLClassLoader, URL }

import scala.collection.JavaConversions.enumerationAsScalaIterator

import de.jowisoftware.mining.importer.Importer
import grizzled.slf4j.Logging

class PluginScanner(pluginDirs: File*) extends Logging {
  def scan(manager: PluginManager) {
    info("Scanning for plugins in: "+pluginDirs.mkString(", "));
    val jarFiles = findFiles

    debug("Possible plugin files: "+jarFiles.mkString(", "));
    var classLoader = loadFiles(jarFiles)
    for (plugin <- getPluginInfos(classLoader))
      manager.addPlugin(plugin)
  }

  private def loadFiles(files: Seq[File]): ClassLoader =
    new URLClassLoader(files.map { _.toURI().toURL() }.toArray, getClass().getClassLoader())

  private def getPluginInfos(classLoader: ClassLoader) =
    mapToPlugins(classLoader, classLoader.getResources("META-INF/MANIFEST.MF"))

  private def findFiles: Seq[File] = {
    def findFiles(oldResults: List[File], dir: File): List[File] = {
      var result = oldResults
      var files = dir.listFiles()
      if (files != null) {
        files.foreach { file =>
          if (file.isDirectory)
            result = findFiles(result, file)
          else if (file.isFile() && file.getName().endsWith(".jar"))
            result = file :: result
        }
      }

      result
    }
    pluginDirs.flatMap(findFiles(Nil, _))
  }

  private def mapToPlugins(classLoader: ClassLoader,
    resources: java.util.Enumeration[URL]): Iterator[Plugin] = {
    resources.flatMap { url =>
      val manifest = new java.util.jar.Manifest(url.openStream())
      val pluginClass = manifest.getMainAttributes().getValue("Plugin-Class")
      val pluginType = manifest.getMainAttributes().getValue("Plugin-Type")
      val pluginName = manifest.getMainAttributes().getValue("Plugin-Name")

      try {
        if (pluginClass != null && pluginType != null && pluginName != null) {
          val clazz = classLoader.loadClass(pluginClass)
          Some(Plugin(PluginType.find(pluginType), pluginName, clazz.asInstanceOf[Class[Importer]]))
        } else {
          None
        }
      } catch {
        case e: Exception =>
          warn("Error while inspecting plugin, skipping it", e)
          None
      }
    }
  }
}