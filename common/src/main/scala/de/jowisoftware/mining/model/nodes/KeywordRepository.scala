package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.nodes.helper.{ MiningNode, HasChildWithName }
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.mining.model.nodes.helper.EmptyNode
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.neo4j.content.IndexAccess

object KeywordRepository extends NodeCompanion[KeywordRepository] {
  def apply = new KeywordRepository
}

class KeywordRepository extends MiningNode with EmptyNode with HasChildWithName[Keyword] with IndexAccess[Keyword] {
  def findOrCreateChild(name: String) =
    Keyword.find(readableDb, name) match {
      case Some(keyword) => keyword
      case None =>
        val node = writableDb.createNode(Keyword)
        node.name(name)
        add(node, Contains)
        node
    }
}