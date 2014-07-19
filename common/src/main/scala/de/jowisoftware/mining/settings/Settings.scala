package de.jowisoftware.mining.settings

import java.io.{FileInputStream, File, InputStream}
import java.util.Properties

import scala.collection.JavaConversions._

object Settings {
  def apply(file: File) = fromStream(new FileInputStream(file))
  def apply(stream: InputStream) = new Settings(stream)
  def apply(fileName: String): Settings = {
    try {
      fromStream(getClass.getClassLoader.getResourceAsStream(fileName))
    }
  }

  private def fromStream(stream: InputStream): Settings =
  {
    try {
      new Settings(stream)
    } finally {
      stream.close()
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
