package de.jowisoftware.neo4j

import org.neo4j.graphdb.{Node => NeoNode, Relationship => NeoRelationship}
import org.neo4j.graphdb.RelationshipType

trait RelationshipCompanion[+T <: Relationship] {
  val relationType: RelationshipType
  
  def apply(): T
  protected[neo4j] type leftType <: Node
  protected[neo4j] type rightType <: Node
} 

trait Relationship extends Versionable with Properties {
  protected[neo4j] type companion <: RelationshipCompanion[Relationship]
  
  private[neo4j] var sourceNode: companion#leftType = _
  private[neo4j] var sinkNode: companion#rightType = _
  private[neo4j] var innerRelationship: NeoRelationship = _
  
  protected[neo4j] def content = innerRelationship
  
  def source = sourceNode
  def sink = sinkNode
  def getRelationship = innerRelationship
  
  def initWith(relationship: NeoRelationship) {
    sanityCheck(relationship)
    this.innerRelationship = relationship
    sourceNode = Node.neoNode2Node(relationship.getStartNode()).get.asInstanceOf[companion#leftType]
    sinkNode = Node.neoNode2Node(relationship.getEndNode()).get.asInstanceOf[companion#rightType]
  }
  
  override def toString() = toString(innerRelationship.getId(), innerRelationship)
  
  def delete = innerRelationship.delete()
}

object Relationship {
  implicit def relationship2RelationshipType(r: RelationshipCompanion[_]): RelationshipType =
    r.relationType
  implicit def relationshipCompanion2RelationshipType(r: Relationship): RelationshipType =
    r.innerRelationship.getType()
  /*
  implicit def relationship2Relationship(r: Relationship): NeoRelationship =
    r.innerRelationship
  */
}