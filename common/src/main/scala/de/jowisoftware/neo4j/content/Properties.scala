package de.jowisoftware.neo4j.content

import java.util.Date
import org.neo4j.graphdb.PropertyContainer
import properties.{ NodeProperty, DateWrapper, CastingObjectPersister, OptionalNodeProperty }
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.index.IndexCreator
import de.jowisoftware.neo4j.content.index.NullIndex

trait Properties[A <: PropertyContainer] {
  protected[neo4j] def content: PropertyContainer
  private[neo4j] val indexCreator: IndexCreator

  private[neo4j] var innerDB: DBWithTransaction[_ <: Node] = _

  protected def stringProperty(name: String, default: String = "", indexed: Boolean = false) =
    new NodeProperty[String, A](this, name, default, index(indexed, name)) with CastingObjectPersister[String]

  protected def intProperty(name: String, default: Int = 0, indexed: Boolean = false) =
    new NodeProperty[Int, A](this, name, default, index(indexed, name)) with CastingObjectPersister[Int]

  protected def floatProperty(name: String, default: Float = 0, indexed: Boolean = false) =
    new NodeProperty[Float, A](this, name, default, index(indexed, name)) with CastingObjectPersister[Float]

  protected def anyProperty(name: String, default: Any = null, indexed: Boolean = false) =
    new NodeProperty[Any, A](this, name, default, index(indexed, name)) with CastingObjectPersister[Any]

  protected def dateProperty(name: String, default: Date = new Date(), indexed: Boolean = false) =
    new NodeProperty[Date, A](this, name, default, index(indexed, name)) with DateWrapper

  protected def optionalStringProperty(name: String, indexed: Boolean = false) =
    new OptionalNodeProperty[String, A](this, name, index(indexed, name)) with CastingObjectPersister[String]

  protected def stringArrayProperty(name: String) =
    new NodeProperty[Array[String], A](this, name, Array[String](), NullIndex) with CastingObjectPersister[Array[String]]

  private def index(realIndex: Boolean, name: String) =
    if (!realIndex) NullIndex
    else indexCreator.create(innerDB, content, getClass().getSimpleName, name)
}