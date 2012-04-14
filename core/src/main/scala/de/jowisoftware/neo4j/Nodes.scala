package de.jowisoftware.neo4j

import org.neo4j.graphdb.{Node => NeoNode, Relationship => NeoRelationship,
  Direction, RelationshipType}
import org.neo4j.graphdb.Traverser.Order
import scala.collection.JavaConversions._

object Node {
  def wrapNeoNode[T <: Node](neoNode: NeoNode, db: DBWithTransaction[_ <: Node])(implicit companion: NodeCompanion[T]): T = {
    val node = companion()
    node initWith(neoNode, db)
    node
  }
  
  def neoNode2Node(node: NeoNode, db: DBWithTransaction[_ <: Node]): Option[Node] = {
    try {
      val clazz = node.getProperty(".class").asInstanceOf[String]
      val obj = Class.forName(clazz).newInstance().asInstanceOf[Node]
      obj initWith(node, db)
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
  private var innerDB: DBWithTransaction[_ <: Node] = _
  
  def content: NeoNode = innerNode
  protected def db = innerDB
  
  private[neo4j] final def initWith(node: NeoNode, db: DBWithTransaction[_ <: Node]) {
    this.innerNode = node
    this.innerDB = db
    sanityCheck(node)
  }
  
  def add[T <: Relationship](other: Node)(implicit relType: RelationshipCompanion[T]): T = {
    val relationship = relType()
    val neoRelationship = innerNode.createRelationshipTo(other.innerNode, relType.relationType)
    relationship.initWith(neoRelationship, db)
    relationship
  }
  
  def neighbors2(direction: Direction=Direction.BOTH, relTypes: Seq[RelationshipCompanion[_]]=List()) =
    neighbors(direction, relTypes.map{_.relationType})

  def neighbors(direction: Direction=Direction.BOTH, relTypes: Seq[RelationshipType]=List()) = {
    val nodes = if (relTypes.isEmpty) innerNode.getRelationships(direction)
    else innerNode.getRelationships(direction, relTypes:_*)
    
    for (Some(node) <- nodes.map(
        n => Node.neoNode2Node(n.getOtherNode(innerNode), innerDB))) yield node
  }

  def getFirstRelationship[T <: Node](direction: Direction=Direction.BOTH, relTypes: RelationshipType)
      (implicit nodeType: NodeCompanion[T]): Option[T] = {
    val targetClass = nodeType.apply().getClass().getName()
    innerNode.getRelationships(relTypes, direction).map {
      _.getOtherNode(innerNode)
    }.find {otherNode =>
        otherNode.hasProperty(".class") && otherNode.getProperty(".class") == targetClass
    } match {
      case Some(node) => Some(Node.wrapNeoNode(node, innerDB))
      case _ => None
    }
  }
  
  def delete = innerNode.delete()
  def forceDelete = {
   innerNode.getRelationships().foreach(_.delete()) 
   delete
  }
  
  override def toString() = toString(innerNode.getId(), innerNode)
  override def hashCode = innerNode.hashCode
  override def equals(other: Any) = other match {
    case n: Node => innerNode.equals(n.innerNode)
    case _ => false
  }
}