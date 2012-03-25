package de.jowisoftware.neo4j

import org.neo4j.graphdb.{Node => NeoNode, Relationship => NeoRelationship}
import scala.collection.JavaConversions._
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.PropertyContainer

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
  
  protected val version: Int
  protected def initProperties = {}
  protected def updateFrom(oldVersion: Int)
}

trait Properties {
  protected[neo4j] def content: PropertyContainer
  protected def stringProperty(name: String) = new NodeProperty[String](this, name)
  protected def intProperty(name: String) = new NodeProperty[Int](this, name)
}

class NodeProperty[T] private[neo4j](val parent: Properties, val name: String) {
  def apply(newValue: T) = {
    parent.content.setProperty(name, obj2Persist(newValue))
  }
  
  def apply(): T = persist2Obj(parent.content.getProperty(name))
  
  def obj2Persist(obj: T): Any = obj
  def persist2Obj(persist: Any): T = persist.asInstanceOf[T]
  
  override def toString = apply().toString
}
