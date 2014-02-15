package de.jowisoftware.neo4j.content

import scala.collection.JavaConversions.iterableAsScalaIterable
import org.neo4j.graphdb.{ Direction, Node => NeoNode, Relationship => NeoRelationship, RelationshipType }
import de.jowisoftware.neo4j.ReadOnlyDatabase
import de.jowisoftware.neo4j.content.index.NodeIndexCreator
import properties.Versionable
import grizzled.slf4j.Logging

object Node extends ClassCache[NodeCompanion[_ <: Node]] {
  def wrapNeoNode[T <: Node](
    neoNode: NeoNode,
    db: ReadOnlyDatabase, companion: NodeCompanion[T]): T = {
    val node = companion()
    node initWith (neoNode, db, companion)
    node
  }

  def wrapNeoNode(node: NeoNode, db: ReadOnlyDatabase): Option[Node] = {
    try {
      val className = node.getProperty("_class").asInstanceOf[String]
      val companion = getCompanion(className)
      val obj = companion.apply()
      obj initWith (node, db, companion)
      Some(obj)
    } catch {
      case e: Exception => None
    }
  }
}

trait Node extends Versionable with Properties[NeoNode] with Logging {
  private[neo4j] var innerNode: NeoNode = _

  private[neo4j] val indexCreator = NodeIndexCreator
  def content: NeoNode = innerNode

  protected final def getIndex = readableDb.service.index.forNodes(getClass().getName)

  /**
    * Initialize a Node with a backing Neo4j Node object.
    * This method is called on initialisation. There is normally no need to
    * call this method manually. It is only public to inject mocks.
    */
  final def initWith(node: NeoNode, db: ReadOnlyDatabase, companion: NodeCompanion[_ <: Node]) = {
    this.innerNode = node
    this.innerDB = db
    sanityCheck(node)

    if (companion.isInstanceOf[IndexedNodeCompanion[_]]) {
      val indexInfo = companion.asInstanceOf[IndexedNodeCompanion[_]].indexInfo
      if (!node.hasLabel(indexInfo.label)) {
        node.addLabel(indexInfo.label)
      }
    }

    this
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
      result.initWith(initRelationship, innerDB)
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

  def relations(direction: Direction = Direction.BOTH, relTypes: Seq[RelationshipType] = List()) = {
    val nodes = if (relTypes.isEmpty) innerNode.getRelationships(direction)
    else innerNode.getRelationships(direction, relTypes: _*)

    nodes.flatMap(n => Node.wrapNeoNode(n.getOtherNode(innerNode), innerDB))
  }

  def neighbors(direction: Direction = Direction.BOTH, relTypes: Seq[RelationshipType] = List()) = {
    val nodes = if (relTypes.isEmpty) innerNode.getRelationships(direction)
    else innerNode.getRelationships(direction, relTypes: _*)

    nodes.flatMap(n => Node.wrapNeoNode(n.getOtherNode(innerNode), innerDB))
  }

  def getFirstNeighbor[A <: Node](direction: Direction = Direction.BOTH,
    relType: RelationshipType, nodeType: NodeCompanion[A]): Option[A] = {

    val targetClass = nodeType.apply().getClass().getName()
    innerNode.getRelationships(relType, direction).find { rel =>
      val otherNode = rel.getOtherNode(innerNode)
      otherNode.hasProperty("_class") && otherNode.getProperty("_class") == targetClass
    } map {
      rel => Node.wrapNeoNode(rel.getOtherNode(innerNode), innerDB, nodeType)
    }
  }

  def getFirstRelationship[A <: Relationship](direction: Direction = Direction.BOTH,
    relCompanion: RelationshipCompanion[A]): Option[A] = {

    val targetClass = relCompanion.apply().getClass().getName()
    innerNode.getRelationships(relCompanion.relationType, direction).find { rel =>
      rel.hasProperty("_class") && rel.getProperty("_class") == targetClass
    } map {
      rel => Relationship.wrapNeoRelationship(rel, innerDB, relCompanion)
    }
  }

  def getOrCreate[A <: Node, B <: Relationship](direction: Direction = Direction.BOTH, relType: RelationshipCompanion[B],
    nodeType: NodeCompanion[A]): A = {

    getFirstNeighbor(direction, relType.relationType, nodeType) getOrElse {
      val node = writableDb.createNode(nodeType)
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