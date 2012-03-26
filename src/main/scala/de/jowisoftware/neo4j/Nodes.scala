package de.jowisoftware.neo4j

import org.neo4j.graphdb.{Node => NeoNode, Relationship => NeoRelationship}
import scala.collection.JavaConversions._
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.Direction

object Node {
  def wrapNeoNode[T <: Node](neoNode: NeoNode)(implicit manifest: Manifest[T]): T = {
    val node = manifest.erasure.newInstance().asInstanceOf[T]
    node initWith neoNode
    node
  }
  
  implicit def node2NeoNode(node: Node): NeoNode = node.content
  
  implicit def neoNode2Node(node: NeoNode): Option[Node] = {
    try {
      val clazz = node.getProperty(".class").asInstanceOf[String]
      val obj = Class.forName(clazz).newInstance().asInstanceOf[Node]
      obj initWith node
      Some(obj)
    } catch {
      case e: Exception => None
    }
  }
}

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
  
  def neighbors() =
    for (Some(node) <- innerNode.getRelationships().map(
        n => Node.neoNode2Node(n.getOtherNode(innerNode)))) yield node
  
  def neighbors(direction: Direction, relType: RelationshipType*) =
    for (Some(node) <- innerNode.getRelationships(direction, relType:_*).map(
        n => Node.neoNode2Node(n.getOtherNode(innerNode)))) yield node

  def neighbors(direction: Direction) =
    for (Some(node) <- innerNode.getRelationships(direction).map(
        n => Node.neoNode2Node(n.getOtherNode(innerNode)))) yield node
        
  def neighbors(relType: RelationshipType*) =
    for (Some(node) <- innerNode.getRelationships(relType:_*).map(
        n => Node.neoNode2Node(n.getOtherNode(innerNode)))) yield node
        
  override def toString() = toString(innerNode.getId(), innerNode)
}

class EmptyNode extends Node {
  protected val version = 1
  protected def updateFrom(oldVersion: Int) = {}
}