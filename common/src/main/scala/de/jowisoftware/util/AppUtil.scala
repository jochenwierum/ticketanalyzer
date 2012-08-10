package de.jowisoftware.util

import java.io.File
import java.io.InputStream
import scala.io.Source
import scala.io.Codec
import java.io.FileInputStream

object AppUtil {
  /**
    * Base path of the application. Points to a directory which contains lib/, bin/ and db/
    * when the app runs in a compiled form. Points to build/classes when running from eclipse.
    */
  lazy val basePath = {
    val file = new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
    if (file.isDirectory)
      file
    else
      file.getParentFile
  }.getParentFile.getCanonicalFile

  def withSettingsSource[T](name: String)(block: Source => T): Option[T] = {
    val file = new File(new File(basePath, "settings"), name)
    if (!file.isFile())
      None
    else
      Option(block(Source.fromFile(file)(Codec.UTF8)))
  }

  def withSettingsInputstream[T](name: String)(block: InputStream => T): Option[T] = {
    val file = new File(new File(basePath, "settings"), name)
    if (!file.isFile()) {
      None
    } else {
      val stream = new FileInputStream(file)
      try {
        Option(block(stream))
      } finally {
        stream.close()
      }
    }
  }
}