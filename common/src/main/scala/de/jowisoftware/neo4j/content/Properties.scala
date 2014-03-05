package de.jowisoftware.neo4j.content

import java.util.Date
import org.neo4j.graphdb.PropertyContainer
import de.jowisoftware.neo4j.{ ReadWriteDatabase, ReadOnlyDatabase }
import de.jowisoftware.neo4j.content.properties.{ OptionalNodeProperty, NodeProperty, CastingObjectPersister }
import properties.{ OptionalNodeProperty, NodeProperty, DateWrapper, CastingObjectPersister }
import de.jowisoftware.neo4j.ReadOnlyDatabase
import de.jowisoftware.neo4j.ReadWriteDatabase

trait Properties[A <: PropertyContainer] {
  protected[neo4j] def content: PropertyContainer

  private[neo4j] var innerDB: ReadOnlyDatabase = _

  protected def readableDb: ReadOnlyDatabase = innerDB
  protected def writableDb: ReadWriteDatabase = innerDB match {
    case e: ReadWriteDatabase => e
    case _ => sys.error("A writeable database requires a transaction")
  }

  protected def stringProperty(name: String, default: String = "") =
    new NodeProperty[String, A](this, name, default) with CastingObjectPersister[String]

  protected def intProperty(name: String, default: Int = 0) =
    new NodeProperty[Int, A](this, name, default) with CastingObjectPersister[Int]

  protected def floatProperty(name: String, default: Float = 0) =
    new NodeProperty[Float, A](this, name, default) with CastingObjectPersister[Float]

  protected def anyProperty(name: String, default: Any = null) =
    new NodeProperty[Any, A](this, name, default) with CastingObjectPersister[Any]

  protected def dateProperty(name: String, default: Date = new Date()) =
    new NodeProperty[Date, A](this, name, default) with DateWrapper

  protected def optionalStringProperty(name: String) =
    new OptionalNodeProperty[String, A](this, name) with CastingObjectPersister[String]

  protected def optionalIntProperty(name: String) =
    new OptionalNodeProperty[Int, A](this, name) with CastingObjectPersister[Int]

  protected def stringArrayProperty(name: String) =
    new NodeProperty[Array[String], A](this, name, Array[String]()) with CastingObjectPersister[Array[String]]

  protected def booleanProperty(name: String, default: Boolean = false) =
    new NodeProperty[Boolean, A](this, name, default) with CastingObjectPersister[Boolean]
}