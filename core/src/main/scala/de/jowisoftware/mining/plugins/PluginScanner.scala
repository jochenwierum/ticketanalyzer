package de.jowisoftware.mining.plugins

import java.io.{ FilenameFilter, File }
import java.net.{ URLClassLoader, URL }
import java.util.regex.Pattern
import scala.Option.option2Iterable
import scala.collection.JavaConversions._
import de.jowisoftware.mining.importer.Importer
import grizzled.slf4j.Logging
import java.util.Properties

class PluginScanner(basePath: File, pluginDirs: String*) extends Logging {
  def scan(manager: PluginManager) {
    info("Scanning in "+basePath+" for "+pluginDirs.mkString(", "))
    val jarFiles = pluginDirs.flatMap(expandPath(basePath, _)).map(_.getCanonicalFile)

    debug("Possible plugin files: "+jarFiles.mkString(", "))
    val classLoader = createClassLoader(jarFiles)
    getPluginInfos(classLoader).foreach(manager.addPlugin)
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

  private def expandPath(base: File, path: String): Seq[File] = {
    val segments = path.split("""\\|/""").toList

    def appendMatchingPathes(base: File, segments: List[String]): List[File] =
      segments match {
        case Nil => base :: Nil
        case head :: tail =>
          if (head.equals("..")) {
            appendMatchingPathes(base.getParentFile(), tail)
          } else if (!head.contains('*')) {
            val file = new File(base, head).getCanonicalFile
            if (file.exists)
              appendMatchingPathes(file, tail)
            else
              Nil
          } else {
            val pattern = Pattern.quote(head)
              .replaceAll("\\?", "\\\\E.\\\\Q")
              .replaceAll("\\*", "\\\\E.*\\\\Q")
              .replaceAll("\\\\Q\\\\E", "")
            val regex = Pattern.compile("^"+pattern+"$", Pattern.CASE_INSENSITIVE)
            val files = base.listFiles(new FilenameFilter() {
              def accept(dir: File, name: String) = {
                regex.matcher(name).find
              }
            })
            if (files != null) {
              files.flatMap { f =>
                appendMatchingPathes(f, tail)
              }.toList
            } else {
              Nil
            }
          }
      }

    appendMatchingPathes(base, segments)
  }
}
