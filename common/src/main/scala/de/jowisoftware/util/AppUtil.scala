package de.jowisoftware.util

import java.io.File

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
  println(basePath.getCanonicalFile)
}