package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.mining.model.nodes.helper.HasIndexedName
import de.jowisoftware.neo4j.content.IndexAccess
import de.jowisoftware.neo4j.DBWithTransaction

object Keyword extends NodeCompanion[Keyword] with IndexAccess[Keyword] {
  def apply = new Keyword

  private[model] def find(db: DBWithTransaction[RootNode], name: String) =
    findInIndex(db, "name", IndexAccess.mask(name), this)
}

class Keyword extends MiningNode with HasIndexedName {
  def version() = 0
  def updateFrom(oldVersion: Int) = {}
}