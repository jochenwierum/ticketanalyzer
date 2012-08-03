package de.jowisoftware.neo4j.content

import org.neo4j.graphdb.{ RelationshipType, Relationship => NeoRelationship }

import de.jowisoftware.neo4j.{ ReadWriteDatabase, ReadOnlyDatabase, Database, DBWithTransaction }
import de.jowisoftware.neo4j.content.index.RelationshipIndexCreator
import properties.Versionable

trait Relationship extends Versionable with Properties[NeoRelationship] {
  private[neo4j]type companion <: RelationshipCompanion[Relationship]
  private[neo4j] val indexCreator = RelationshipIndexCreator

  private[neo4j] var cachedSourceNode: Option[companion#sourceType] = None
  private[neo4j] var cachedSinkNode: Option[companion#sinkType] = None
  private[neo4j] var innerRelationship: NeoRelationship = _

  protected[neo4j] def content = innerRelationship

  def sink = cachedSinkNode match {
    case Some(node) => node
    case None =>
      val node = Node.neoNode2Node(innerRelationship.getEndNode(), innerDB).get.asInstanceOf[companion#sinkType]
      cachedSinkNode = Option(node)
      node
  }

  def source = cachedSourceNode match {
    case Some(node) => node
    case None =>
      val node = Node.neoNode2Node(innerRelationship.getStartNode(), innerDB).get.asInstanceOf[companion#sourceType]
      cachedSourceNode = Option(node)
      node
  }

  def relationship = innerRelationship

  protected final def getIndex = readableDb.service.index.forRelationships(getClass().getName)

  def initWith(relationship: NeoRelationship, db: ReadOnlyDatabase[_ <: Node]) {
    sanityCheck(relationship)
    this.innerRelationship = relationship
    this.innerDB = db
  }

  override def toString() = toString(innerRelationship.getId(), innerRelationship)

  def delete = innerRelationship.delete()
}

object Relationship {
  implicit def relationship2RelationshipType(r: RelationshipCompanion[_]): RelationshipType =
    r.relationType
  implicit def relationshipCompanion2RelationshipType(r: Relationship): RelationshipType =
    r.innerRelationship.getType()
}