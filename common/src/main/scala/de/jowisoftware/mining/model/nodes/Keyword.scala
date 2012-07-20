package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.nodes.helper.{MiningNode, HasIndexedName}
import de.jowisoftware.neo4j.ReadOnlyDatabase
import de.jowisoftware.neo4j.content.{NodeCompanion, IndexAccess}

object Keyword extends NodeCompanion[Keyword] with IndexAccess[Keyword] {
  def apply = new Keyword

  private[model] def find(db: ReadOnlyDatabase[RootNode], name: String) =
    findInIndex(db, "name", IndexAccess.mask(name), this)
}

class Keyword extends MiningNode with HasIndexedName {
  def version() = 0
  def updateFrom(oldVersion: Int) = {}
}