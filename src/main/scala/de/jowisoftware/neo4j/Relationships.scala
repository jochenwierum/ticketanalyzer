package de.jowisoftware.neo4j

import org.neo4j.graphdb.{Node => NeoNode, Relationship => NeoRelationship}
import org.neo4j.graphdb.RelationshipType

trait Relationship extends Versionable with Properties {
  val relationType: RelationshipType
  
  protected type leftType <: Node
  protected type rightType <: Node
  protected val leftTypeManifest: Manifest[leftType]
  protected val rightTypeManifest: Manifest[rightType]
  
  private[neo4j] var sourceNode: leftType = _
  private[neo4j] var sinkNode: rightType = _
  private[neo4j] var innerRelationship: NeoRelationship = _
  
  protected[neo4j] def content = innerRelationship
  
  def source = sourceNode
  def sink = sinkNode
  def getRelationship = innerRelationship
  
  def initWith(relationship: NeoRelationship) {
    sanityCheck(relationship)
    this.innerRelationship = relationship
    sourceNode = Converter.neoNode2Node(relationship.getStartNode())(leftTypeManifest)
    sinkNode = Converter.neoNode2Node(relationship.getEndNode())(rightTypeManifest)
  }
  
  override def toString() = toString(innerRelationship.getId(), innerRelationship)
}