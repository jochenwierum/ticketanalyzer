package de.jowisoftware.mining.model.helper

import de.jowisoftware.neo4j.content.RelationshipCompanion
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.neo4j.content.Relationship
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model._

protected trait HasChildWithName[A <: HasName] extends MiningNode {
  protected def findOrCreateChild(name: String,
    relationShip: RelationshipCompanion[_ <: Relationship], creator: NodeCompanion[A]): A = {

    val neighbor = neighbors(Direction.OUTGOING, List(relationShip.relationType)).find {
      _ match {
        case node: HasName => node.name() == name
      }
    }

    neighbor match {
      case Some(node) => node.asInstanceOf[A]
      case None =>
        val node = db.createNode(creator)
        node.name(name)
        add(node)(relationShip)
        node
    }
  }

  def findOrCreateChild(name: String): A
}