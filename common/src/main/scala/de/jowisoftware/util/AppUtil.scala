package de.jowisoftware.util

import java.io.File
import java.io.InputStream
import scala.io.Source
import scala.io.Codec
import java.io.FileInputStream
import de.jowisoftware.mining.settings.Settings
import grizzled.slf4j.Logging

object AppUtil extends Logging {
  /**
    * Base path of the application. Points to a directory which contains lib/, bin/ and db/
    * when the app runs in a compiled form. Points to build/classes when running from eclipse.
    */
  lazy val basePath = {
    val sourceCodeFile = new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
    val parentDir = if (sourceCodeFile.isDirectory)
      sourceCodeFile.getParentFile
    else
      sourceCodeFile.getParentFile.getParentFile

    // This "hack" is for eclipse, where a "classes" directory is in the path, too
    val baseDir = if (parentDir.getName.toLowerCase() == "classes")
      parentDir.getParentFile()
    else
      parentDir

    val projectDir = baseDir.getCanonicalFile
    info("Found project dir at "+projectDir)
    projectDir
  }

  lazy val appSettings = Settings("config.properties")
  lazy val defaultSettings = loadSettings("defaults.properties") getOrElse Settings.empty

  def projectFile(name: String) = new File(basePath, name).getCanonicalFile()

  def withSettingsSource[T](name: String)(block: Source => T): Option[T] =
    findSettings(name) map { file =>
      block(Source.fromFile(file)(Codec.UTF8))
    }

  def loadSettings[T](name: String): Option[Settings] =
    findSettings(name) map { file =>
      val stream = new FileInputStream(file)
      try {
        Settings(stream)
      } finally {
        stream.close()
      }
    }

  def findSettings(name: String): Option[File] =
    appSettings.getArray("settingsdirs").flatMap(FileUtils.expandPath(basePath, _)) map {
      new File(_, name)
    } find (_.exists)
}