package de.jowisoftware.neo4j.content

import scala.language.implicitConversions

import org.neo4j.graphdb.{ RelationshipType, Relationship => NeoRelationship }
import de.jowisoftware.neo4j.{ ReadWriteDatabase, ReadOnlyDatabase, Database, DBWithTransaction }
import properties.Versionable
import org.xml.sax.helpers.NewInstance

object Relationship extends ClassCache[RelationshipCompanion[_ <: Relationship]] {
  implicit def relationship2RelationshipType(r: RelationshipCompanion[_]): RelationshipType =
    r.relationType
  implicit def relationshipCompanion2RelationshipType(r: Relationship): RelationshipType =
    r.innerRelationship.getType()

  def wrapNeoRelationship[T <: Relationship](
    neoNode: NeoRelationship,
    db: ReadOnlyDatabase, companion: RelationshipCompanion[T]): T = {
    val node = companion()
    node initWith (neoNode, db)
    node
  }

  def wrapNeoRelationship(relationship: NeoRelationship, db: ReadOnlyDatabase): Option[Relationship] = {
    try {
      val className = relationship.getProperty("_class").asInstanceOf[String]
      val obj = getCompanion(className).apply()
      obj initWith (relationship, db)
      Some(obj)
    } catch {
      case e: Exception => None
    }
  }
}

trait Relationship extends Versionable with Properties[NeoRelationship] {
  private[neo4j]type companion <: RelationshipCompanion[Relationship]

  private[neo4j] var cachedSourceNode: Option[companion#sourceType] = None
  private[neo4j] var cachedSinkNode: Option[companion#sinkType] = None
  private[neo4j] var innerRelationship: NeoRelationship = _

  protected[neo4j] def content = innerRelationship

  def sink = cachedSinkNode match {
    case Some(node) => node
    case None =>
      val node = Node.wrapNeoNode(innerRelationship.getEndNode(), innerDB).get.asInstanceOf[companion#sinkType]
      cachedSinkNode = Option(node)
      node
  }

  def source = cachedSourceNode match {
    case Some(node) => node
    case None =>
      val node = Node.wrapNeoNode(innerRelationship.getStartNode(), innerDB).get.asInstanceOf[companion#sourceType]
      cachedSourceNode = Option(node)
      node
  }

  def relationship = innerRelationship

  protected final def getIndex = readableDb.service.index.forRelationships(getClass().getName)

  def initWith(relationship: NeoRelationship, db: ReadOnlyDatabase) = {
    sanityCheck(relationship)
    this.innerRelationship = relationship
    this.innerDB = db
    this
  }

  override def toString() = toString(innerRelationship.getId(), innerRelationship)

  def delete() = innerRelationship.delete()
}