package de.jowisoftware.neo4j.content

import java.util.Date
import org.neo4j.graphdb.PropertyContainer
import properties.{NodeProperty, DateWrapper, CastingObjectPersister, OptionalNodeProperty}
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.index.IndexCreator
import de.jowisoftware.neo4j.content.index.NullIndex

trait Properties[A <: PropertyContainer] {
  protected[neo4j] def content: PropertyContainer
  private[neo4j] val indexCreator: IndexCreator

  private[neo4j] var innerDB: DBWithTransaction[_ <: Node] = _

  protected def stringProperty(name: String, default: String = "", indexName: Option[String] = None) =
    new NodeProperty[String, A](this, name, default, index(indexName)) with CastingObjectPersister[String]

  protected def intProperty(name: String, default: Int = 0, indexName: Option[String] = None) =
    new NodeProperty[Int, A](this, name, default, index(indexName)) with CastingObjectPersister[Int]

  protected def anyProperty(name: String, default: Any = null, indexName: Option[String] = None) =
    new NodeProperty[Any, A](this, name, default, index(indexName)) with CastingObjectPersister[Any]

  protected def dateProperty(name: String, default: Date = new Date(), indexName: Option[String] = None) =
    new NodeProperty[Date, A](this, name, default, index(indexName)) with DateWrapper

  protected def optionalStringProperty(name: String, indexName: Option[String] = None) = {
    new OptionalNodeProperty[String, A](this, name, index(indexName)) with CastingObjectPersister[String]
  }

  private def index(indexName: Option[String]) = indexName match {
    case None => NullIndex
    case Some(name) => indexCreator.create(innerDB.service, content, name);
  }
}