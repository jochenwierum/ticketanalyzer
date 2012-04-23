package de.jowisoftware.neo4j.content

import org.neo4j.graphdb.{RelationshipType, Relationship => NeoRelationship}

import de.jowisoftware.neo4j.DBWithTransaction
import properties.Versionable

trait Relationship extends Versionable with Properties {
  protected[neo4j] type companion <: RelationshipCompanion[Relationship]

  private[neo4j] var sourceNode: companion#sourceType = _
  private[neo4j] var sinkNode: companion#sinkType = _
  private[neo4j] var innerRelationship: NeoRelationship = _

  protected[neo4j] def content = innerRelationship

  def source = sourceNode
  def sink = sinkNode
  def getRelationship = innerRelationship

  def initWith(relationship: NeoRelationship, db: DBWithTransaction[_ <: Node]) {
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