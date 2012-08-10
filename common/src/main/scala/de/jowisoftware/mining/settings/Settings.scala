package de.jowisoftware.mining.settings

import scala.io.Source
import java.util.Properties
import java.io.InputStream
import scala.collection.JavaConversions._
import java.io.File
import java.io.FileInputStream

object Settings {
  def apply(stream: InputStream) = new Settings(stream)
  def apply(fileName: String): Settings = apply(fileName, getClass().getClassLoader)

  def apply(file: String, classLoader: ClassLoader): Settings = {
    val stream = classLoader.getResourceAsStream(file)
    try {
      new Settings(stream)
    } finally {
      stream.close
    }
  }

  val empty = new Settings(null)
}

class Settings private (stream: InputStream) {
  private val properties = loadProperties

  def getOptionString(key: String): Option[String] = Option(properties.getProperty(key))
  def getString(key: String, default: String) = properties.getProperty(key, default)

  def getString(key: String) = getOptionString(key) match {
    case Some(value) => value
    case None => sys.error("Setting '"+key+"' is missing and no default is provided")
  }

  def getArray(key: String): Array[String] = {
    def value = getString(key)

    if (value == null)
      Array()
    else
      value.split(";")
  }

  private def loadProperties: Properties = {
    val properties = new Properties()
    if (stream != null)
      properties.load(stream)
    properties
  }

  def propertyNames = properties.stringPropertyNames().toSet
}