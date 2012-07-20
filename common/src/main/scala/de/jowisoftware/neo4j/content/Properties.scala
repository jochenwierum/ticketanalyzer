package de.jowisoftware.neo4j.content

import java.util.Date
import org.neo4j.graphdb.PropertyContainer
import de.jowisoftware.neo4j.{ ReadWriteDatabase, ReadOnlyDatabase }
import de.jowisoftware.neo4j.content.index.{ NullIndex, IndexCreator }
import de.jowisoftware.neo4j.content.properties.{ OptionalNodeProperty, NodeProperty, CastingObjectPersister }
import properties.{ OptionalNodeProperty, NodeProperty, DateWrapper, CastingObjectPersister }
import de.jowisoftware.neo4j.ReadOnlyDatabase
import de.jowisoftware.neo4j.ReadWriteDatabase

trait Properties[A <: PropertyContainer] {
  protected[neo4j] def content: PropertyContainer
  private[neo4j] val indexCreator: IndexCreator

  private[neo4j] var innerDB: ReadOnlyDatabase[_ <: Node] = _

  protected def readableDb: ReadOnlyDatabase[_ <: Node] = innerDB
  protected def writableDb: ReadWriteDatabase[_ <: Node] = innerDB match {
    case e: ReadWriteDatabase[_] => e
    case _ => sys.error("A writeable database requires a transaction")
  }

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

  protected def booleanProperty(name: String, default: Boolean = false) =
    new NodeProperty[Boolean, A](this, name, default, NullIndex) with CastingObjectPersister[Boolean]

  private def index(realIndex: Boolean, name: String) =
    if (!realIndex) NullIndex
    else indexCreator.create(innerDB.service, content, getClass().getSimpleName, name)
}