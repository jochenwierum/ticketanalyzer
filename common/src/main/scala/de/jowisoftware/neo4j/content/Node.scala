package de.jowisoftware.neo4j.content

import org.neo4j.graphdb.{
  Node => NeoNode,
  Relationship => NeoRelationship,
  Direction,
  RelationshipType
}
import org.neo4j.graphdb.Traverser.Order
import scala.collection.JavaConversions._
import properties.Versionable
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.index.NodeIndexCreator

object Node {
  def wrapNeoNode[T <: Node](
    neoNode: NeoNode,
    db: DBWithTransaction[_ <: Node], companion: NodeCompanion[T]): T = {
    val node = companion()
    node initWith (neoNode, db)
    node
  }

  def neoNode2Node(node: NeoNode, db: DBWithTransaction[_ <: Node]): Option[Node] = {
    try {
      val clazz = node.getProperty("_class").asInstanceOf[String]
      val obj = Class.forName(clazz).newInstance().asInstanceOf[Node]
      obj initWith (node, db)
      Some(obj)
    } catch {
      case e: Exception => None
    }
  }
}

trait Node extends Versionable with Properties[NeoNode] {
  private[neo4j] var innerNode: NeoNode = _
  private[neo4j] val indexCreator = NodeIndexCreator

  def content: NeoNode = innerNode
  protected def db = innerDB

  protected final def getIndex = db.service.index.forNodes(getClass().getName)

  /**
    * Initialize a Node with a backing Neo4j Node object.
    * This method is called on initialisation. There is normally no need to
    * call this method manually. It is only public to inject mocks.
    */
  final def initWith(node: NeoNode, db: DBWithTransaction[_ <: Node]) {
    this.innerNode = node
    this.innerDB = db
    sanityCheck(node)
  }

  /**
    * Adds a relationship to another node.
    * If such a relationship already exists and the
    * {@link de.jowisoftware.neo4j.content.RelationshipCompanion} does not allow
    * duplicates, the existing relationship is reused and returned. Otherwise
    * a new relationship will be created and returned.
    */
  def add[T <: Relationship](other: Node, relType: RelationshipCompanion[T]): T = {
    def createRelationship(neoRelationship: Option[NeoRelationship]) = {
      val result = relType()
      val initRelationship = neoRelationship match {
        case Some(relationship) => relationship
        case None =>
          innerNode.createRelationshipTo(other.innerNode, relType.relationType)
      }
      result.initWith(initRelationship, db)
      result
    }

    if (!relType.allowDuplicates) {
      val relations = innerNode.getRelationships(relType.relationType, Direction.OUTGOING)
      val existingRelation = relations.find(_.getEndNode.getId == other.innerNode.getId)
      createRelationship(existingRelation)
    } else {
      createRelationship(None)
    }
  }

  def neighbors2(direction: Direction = Direction.BOTH, relTypes: Seq[RelationshipCompanion[_]] = List()) =
    neighbors(direction, relTypes.map { _.relationType })

  def neighbors(direction: Direction = Direction.BOTH, relTypes: Seq[RelationshipType] = List()) = {
    val nodes = if (relTypes.isEmpty) innerNode.getRelationships(direction)
    else innerNode.getRelationships(direction, relTypes: _*)

    for (
      Some(node) <- nodes.map(
        n => Node.neoNode2Node(n.getOtherNode(innerNode), innerDB))
    ) yield node
  }

  def getFirstNeighbor[A <: Node](direction: Direction = Direction.BOTH,
    relType: RelationshipType, nodeType: NodeCompanion[A]): Option[A] = {

    val targetClass = nodeType.apply().getClass().getName()
    innerNode.getRelationships(relType, direction).find { rel =>
      val otherNode = rel.getOtherNode(innerNode)
      otherNode.hasProperty("_class") && otherNode.getProperty("_class") == targetClass
    } match {
      case Some(node) => Some(Node.wrapNeoNode(node.getOtherNode(innerNode), innerDB, nodeType))
      case _ => None
    }
  }

  def getOrCreate[A <: Node, B <: Relationship](direction: Direction = Direction.BOTH, relType: RelationshipCompanion[B],
    nodeType: NodeCompanion[A]): A = {

    getFirstNeighbor(direction, relType.relationType, nodeType) getOrElse {
      val node = db.createNode(nodeType)
      if (direction == Direction.INCOMING) {
        node.add(this, relType)
      } else {
        this.add(node, relType)
      }
      node
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