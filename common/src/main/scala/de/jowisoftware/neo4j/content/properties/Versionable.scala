package de.jowisoftware.neo4j.content.properties

import org.neo4j.graphdb.PropertyContainer
import scala.collection.JavaConversions.iterableAsScalaIterable

trait Versionable {
  private[neo4j] final def sanityCheck(content: PropertyContainer) {
    if (content.hasProperty("_version")) {
      checkProperties(content)
    } else {
      initPropertiesInternal(content)
    }
  }

  private def initPropertiesInternal(content: PropertyContainer) {
    content.setProperty("_class", getClass.getName())
    content.setProperty("_version", version)
    initProperties
  }

  private def checkProperties(content: PropertyContainer) {
    val className = content.getProperty("_class")
    require(className == getClass.getName())

    val nodeVersion = content.getProperty("_version").asInstanceOf[Int]

    if (nodeVersion < version) {
      updateFrom(nodeVersion)
      content.setProperty("_version", version)
    }
  }

  protected def toString(id: Long, content: PropertyContainer): String =
    content.getPropertyKeys().map({ key => key+"="+content.getProperty(key) }).
      mkString("["+getClass.getSimpleName+" "+id+": ", ", ", "]")

  protected def version: Int
  protected def initProperties = {}
  protected def updateFrom(oldVersion: Int)
}
