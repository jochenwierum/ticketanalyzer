package de.jowisoftware.neo4j

import org.neo4j.graphdb.{Node => NeoNode, Relationship => NeoRelationship}
import scala.collection.JavaConversions._
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.PropertyContainer
import java.util.Date
import java.text.SimpleDateFormat

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

trait Properties {
  protected[neo4j] def content: PropertyContainer
  protected def stringProperty(name: String) = new NodeProperty[String](this, name) with CastingObjectPersister[String]
  protected def intProperty(name: String) = new NodeProperty[Int](this, name) with CastingObjectPersister[Int]
  protected def anyProperty(name: String) = new NodeProperty[Any](this, name) with CastingObjectPersister[Any]
  protected def dateProperty(name: String) = new NodeProperty[Date](this, name) with DateWrapper

  protected def optionalStringProperty(name: String) = new OptionalNodeProperty[String](this, name) with CastingObjectPersister[String]
}

abstract class NodeProperty[T] private[neo4j](val parent: Properties, val name: String)
    extends ObjectPersister[T] {
  def apply(newValue: T) = {
    parent.content.setProperty(name, obj2Persist(newValue))
  }

  def apply(): T = persist2Obj(parent.content.getProperty(name))

  override def hashCode = apply().hashCode
  override def equals(other: Any) = apply().equals(other)

  override def toString = apply().toString
}

abstract class OptionalNodeProperty[T](val parent: Properties, val name: String)
    extends ObjectPersister[T] {
  def apply(newValue: Option[T]): Unit = {
    newValue match {
      case Some(x) => parent.content.setProperty(name, obj2Persist(x))
      case None => parent.content.removeProperty(name)
    }
  }

  def apply(): Option[T] = {
    if (parent.content.hasProperty(name))
      Some(persist2Obj(parent.content.getProperty(name)))
    else
      None
  }

  override def hashCode = apply() match {
    case Some(x) => x.hashCode()
    case None => 0
  }

  override def equals(other: Any) = apply() match {
    case Some(x) => x.equals(other)
    case None => other.equals(None)
  }

  override def toString = apply() match {
    case Some(x) => x.toString
    case None => "(Undefined)"
  }
}

private[neo4j] trait ObjectPersister[T] {
  def obj2Persist(obj: T): Any
  def persist2Obj(persist: Any): T
}

private[neo4j] trait CastingObjectPersister[T] extends ObjectPersister[T] {
  def obj2Persist(obj: T): Any = obj
  def persist2Obj(persist: Any): T = persist.asInstanceOf[T]
}

private[neo4j] object DateWrapper {
  val FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
}

private[neo4j] trait DateWrapper extends ObjectPersister[Date] {
  def obj2Persist(obj: Date): Any = DateWrapper.FORMAT.format(obj)
  def persist2Obj(persist: Any): Date = DateWrapper.FORMAT.parse(persist.toString)
}