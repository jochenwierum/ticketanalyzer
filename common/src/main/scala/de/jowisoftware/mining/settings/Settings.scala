package de.jowisoftware.mining.settings

import scala.io.Source
import java.util.Properties
import java.io.InputStream
import scala.collection.JavaConversions._

object Settings {
  def apply(stream: InputStream) = new Settings(stream)
  def apply(file: String): Settings = apply(file, getClass().getClassLoader)
  def apply(file: String, classLoader: ClassLoader): Settings = {
    val stream = classLoader.getResourceAsStream(file)
    try {
      new Settings(stream)
    } finally {
      stream.close
    }
  }
}

class Settings private (stream: InputStream) {
  private val properties = loadProperties

  def getString(key: String) = properties.getProperty(key)
  def getString(key: String, default: String) = properties.getProperty(key, default)

  def getArray(key: String): Array[String] = {
    def value = getString(key)

    if (value == null)
      Array()
    else
      value.split(";")
  }

  private def loadProperties: Properties = {
    val properties = new Properties()
    properties.load(stream)
    properties
  }

  def propertyNames = properties.stringPropertyNames().toSet
}