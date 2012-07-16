package de.jowisoftware.mining.model.nodes.helper

import de.jowisoftware.neo4j.content.RelationshipCompanion
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.neo4j.content.Relationship
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model._

protected trait HasChildWithName[A <: HasName] extends MiningNode {
  protected def findOrCreateChild(name: String,
    relationship: RelationshipCompanion[_ <: Relationship], creator: NodeCompanion[A]): A = {

    val neighbor = neighbors(Direction.OUTGOING, List(relationship.relationType)).find {
      _ match {
        case node: HasName => node.name() == name
      }
    }

    neighbor match {
      case Some(node) => node.asInstanceOf[A]
      case None =>
        val node = db.createNode(creator)
        node.name(name)
        add(node, relationship)
        node
    }
  }

  def findOrCreateChild(name: String): A
}