package de.jowisoftware.neo4j.content

import java.util.Date
import org.neo4j.graphdb.PropertyContainer
import properties.{NodeProperty, DateWrapper, CastingObjectPersister, OptionalNodeProperty}
import de.jowisoftware.neo4j.DBWithTransaction

trait Properties {
  protected[neo4j] def content: PropertyContainer
  protected[neo4j] var innerDB: DBWithTransaction[_ <: Node] = _
  
  protected def stringProperty(name: String) = new NodeProperty[String](this, name) with CastingObjectPersister[String]
  protected def intProperty(name: String) = new NodeProperty[Int](this, name) with CastingObjectPersister[Int]
  protected def anyProperty(name: String) = new NodeProperty[Any](this, name) with CastingObjectPersister[Any]
  protected def dateProperty(name: String) = new NodeProperty[Date](this, name) with DateWrapper

  protected def optionalStringProperty(name: String) = new OptionalNodeProperty[String](this, name) with CastingObjectPersister[String]
}