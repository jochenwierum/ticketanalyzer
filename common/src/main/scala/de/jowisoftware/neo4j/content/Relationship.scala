package de.jowisoftware.neo4j.content

import org.neo4j.graphdb.{ RelationshipType, Relationship => NeoRelationship }

import de.jowisoftware.neo4j.{ ReadWriteDatabase, ReadOnlyDatabase, Database, DBWithTransaction }
import de.jowisoftware.neo4j.content.index.RelationshipIndexCreator
import properties.Versionable

trait Relationship extends Versionable with Properties[NeoRelationship] {
  private[neo4j]type companion <: RelationshipCompanion[Relationship]
  private[neo4j] val indexCreator = RelationshipIndexCreator

  private[neo4j] var sourceNode: companion#sourceType = _
  private[neo4j] var sinkNode: companion#sinkType = _
  private[neo4j] var innerRelationship: NeoRelationship = _

  protected[neo4j] def content = innerRelationship

  def source = sourceNode
  def sink = sinkNode
  def getRelationship = innerRelationship

  protected final def getIndex = readableDb.service.index.forRelationships(getClass().getName)

  def initWith(relationship: NeoRelationship, db: ReadOnlyDatabase[_ <: Node]) {
    sanityCheck(relationship)
    this.innerRelationship = relationship
    this.innerDB = db
    sourceNode = Node.neoNode2Node(relationship.getStartNode(), db).get.asInstanceOf[companion#sourceType]
    sinkNode = Node.neoNode2Node(relationship.getEndNode(), db).get.asInstanceOf[companion#sinkType]
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