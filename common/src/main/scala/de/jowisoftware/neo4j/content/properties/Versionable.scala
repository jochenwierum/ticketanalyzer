package de.jowisoftware.neo4j.content.properties

import org.neo4j.graphdb.PropertyContainer
import scala.collection.JavaConversions.iterableAsScalaIterable

trait Versionable {
  private[neo4j] final def sanityCheck(content: PropertyContainer) {
    if (content.hasProperty(".version")) {
      checkProperties(content)
    } else {
      initPropertiesInternal(content)
    }
  }

  private def initPropertiesInternal(content: PropertyContainer) {
    content.setProperty(".class", getClass.getName())
    content.setProperty(".version", version)
    initProperties
  }

  private def checkProperties(content: PropertyContainer) {
    val className = content.getProperty(".class")
    require(className == getClass.getName())

    val nodeVersion = content.getProperty(".version").asInstanceOf[Int]

    if (nodeVersion < version) {
      updateFrom(nodeVersion)
      content.setProperty(".version", version);
    }
  }

  protected def toString(id: Long, content: PropertyContainer): String =
    content.getPropertyKeys().map({key => key +"="+ content.getProperty(key)}).
        mkString("["+ getClass.getSimpleName +" "+ id +": ", ", ", "]")

  protected def version: Int
  protected def initProperties = {}
  protected def updateFrom(oldVersion: Int)
}
