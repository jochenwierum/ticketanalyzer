package de.jowisoftware.mining.settings
import scala.io.Source
import java.util.Properties

class Settings {
  private val properties = loadProperties

  def getString(key: String) = properties.getProperty(key)
  def getString(key: String, default: String) = properties.getProperty(key, default)

  def getArray(key: String): Array[String] = {
    def value = getString(key)

    if (value == null)
      return Array()

    value.split(";")
  }

  private def loadProperties = {
    val stream = getClass().getClassLoader().getResourceAsStream("config.properties")
    val properties = new Properties()
    properties.load(stream)
    stream.close()

    properties
  }
}