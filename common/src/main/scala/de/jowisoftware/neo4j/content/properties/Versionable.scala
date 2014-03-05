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
    require(className == getClass.getName(), "Expected node '"+getClass.getName+"' had type '"+
      className+"'")

    val nodeVersion = content.getProperty("_version").asInstanceOf[Int]

    if (nodeVersion < version) {
      updateFrom(nodeVersion)
      content.setProperty("_version", version)
    }
  }

  protected def toString(id: Long, content: PropertyContainer): String = {
    if(content.hasProperty("uid") && content.getProperty("uid").isInstanceOf[Set[_]])
        println(content.getProperty("uid"))

    content.getPropertyKeys()
      .filter(!_.startsWith("_"))
      .map(key => key+"="+shorten(content.getProperty(key)))
      .mkString("["+getClass.getSimpleName+" "+id+": ", ", ", "]")
    }

  private def shorten(o: Object) = o match {
    case s: String if s.length > 32 => '"' + s.substring(0, 29)+"...\""
    case s: String => '"' + s + '"'
    case o => o.toString
  }

  def version: Int
  protected def initProperties = {}
  protected def updateFrom(oldVersion: Int)
}
