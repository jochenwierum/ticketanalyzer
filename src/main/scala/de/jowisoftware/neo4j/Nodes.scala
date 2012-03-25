package de.jowisoftware.neo4j

import org.neo4j.graphdb.{Node => NeoNode, Relationship => NeoRelationship}

trait Node extends Versionable with Properties {
  protected[neo4j] var innerNode: NeoNode = _
  
  protected[neo4j] def content: NeoNode = innerNode
  
  private[neo4j] final def initWith(node: NeoNode) {
    this.innerNode = node
    sanityCheck(node)
  }
  
  def add[R <: Relationship](other: Node)(implicit man: Manifest[R]): R = {
    require(classOf[Relationship].isAssignableFrom(man.erasure))
    
    val relationship = man.erasure.newInstance().asInstanceOf[R]
    val neoRelationship = innerNode.createRelationshipTo(other.innerNode, relationship.relationType)
    relationship initWith neoRelationship
    relationship
  }
  
  /*
  def neighbors(relType: RelationshipType, direction: Direction): Seq[Node] {
    
  }
  */
  
  override def toString() = toString(innerNode.getId(), innerNode)
}

class EmptyNode extends Node {
  protected val version = 1
  protected def updateFrom(oldVersion: Int) = {}
}