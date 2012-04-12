package de.jowisoftware.mining.plugins
import java.io.File
import grizzled.slf4j.Logging
import java.net.URLClassLoader
import scala.collection.JavaConversions._
import de.jowisoftware.mining.importer.Importer
import scala.annotation.tailrec
import java.net.URL

class PluginScanner(pluginDir: File) extends Logging {
  def scan(manager: PluginManager) {
    info("Scanning for plugins");
    val jarFiles = findFiles
    
    debug("Possible plugin files: "+ jarFiles.mkString(", "));
    var classLoader = loadFiles(jarFiles)
    for (plugin <- getPluginInfos(classLoader))
      manager.addPlugin(plugin)
  }
  
  private def loadFiles(files: Seq[File]): ClassLoader =
      new URLClassLoader(files.map{_.toURI().toURL()}.toArray, getClass().getClassLoader())
  
  private def getPluginInfos(classLoader: ClassLoader) = {
    val resources = classLoader.getResources("META-INF/MANIFEST.MF")
    val plugins = mapToPlugins(classLoader, resources)
    plugins.filter{_ match {
      case Some(plugin) => true
      case None => false
    }}.map {_.get}
  }
  
  private def findFiles: List[File] = {
    def findFiles(oldResults: List[File], dir: File): List[File] = {
      var result = oldResults
      dir.listFiles().foreach { file =>
        if (file.isDirectory)
          result = findFiles(result, file)
        else if(file.isFile() && file.getName().endsWith(".jar"))
          result = file :: result
      }
      
      result
    }
    return findFiles(Nil, pluginDir)
  }
  
  private def mapToPlugins(classLoader: ClassLoader, 
      resources: java.util.Enumeration[URL]): Iterator[Option[Plugin]] = {
    resources.map { url =>
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