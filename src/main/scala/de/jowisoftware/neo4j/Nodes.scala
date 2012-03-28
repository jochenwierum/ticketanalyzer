package de.jowisoftware.neo4j

import org.neo4j.graphdb.{Node => NeoNode, Relationship => NeoRelationship,
  Direction, RelationshipType}
import org.neo4j.graphdb.Traverser.Order

import scala.collection.JavaConversions._

object Node {
  def wrapNeoNode[T <: Node](neoNode: NeoNode)(implicit companion: NodeCompanion[T]): T = {
    val node = companion()
    node initWith neoNode
    node
  }
  
  implicit def node2NeoNode(node: Node): NeoNode = node.content
  
  def neoNode2Node(node: NeoNode): Option[Node] = {
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

trait NodeCompanion[+T <: Node] {
  def apply() : T
}

trait Node extends Versionable with Properties {
  protected[neo4j] var innerNode: NeoNode = _
  
  protected[neo4j] def content: NeoNode = innerNode
  
  private[neo4j] final def initWith(node: NeoNode) {
    this.innerNode = node
    sanityCheck(node)
  }
  
  def add[T <: Relationship](other: Node)(implicit relType: RelationshipCompanion[T]): T = {
    val relationship = relType()
    val neoRelationship = innerNode.createRelationshipTo(other.innerNode, relType.relationType)
    relationship initWith neoRelationship
    relationship
  }
  
  def neighbors2(direction: Direction=Direction.BOTH, relTypes: Seq[RelationshipCompanion[_]]=List()) =
    neighbors(direction, relTypes.map{_.relationType})

  def neighbors(direction: Direction=Direction.BOTH, relTypes: Seq[RelationshipType]=List()) = {
    val nodes = if (relTypes.isEmpty) innerNode.getRelationships(direction)
    else innerNode.getRelationships(direction, relTypes:_*)
    
    for (Some(node) <- nodes.map(
        n => Node.neoNode2Node(n.getOtherNode(innerNode)))) yield node
  }
  
  /*
  def traverse(order: Order, stopEvaluator: NeoStopEvaluator, returnableEvaluator: NeoReturnableEvaluator,
      relationsAndDirections: Map[RelationshipType, Direction]) = {
 
    val relationsAndDirectionsObject = relationsAndDirections.map(x => List(x._1, x._2)).toSeq
    innerNode.traverse(order, stopEvaluator, returnableEvaluator, relationsAndDirectionsObject:_*)
  }
  */
  
  override def toString() = toString(innerNode.getId(), innerNode)
}

class EmptyNode extends Node {
  protected val version = 1
  protected def updateFrom(oldVersion: Int) = {}
}