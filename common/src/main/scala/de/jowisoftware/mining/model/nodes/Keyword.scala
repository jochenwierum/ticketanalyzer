package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.nodes.helper.{ MiningNode, HasIndexedName }
import de.jowisoftware.neo4j.ReadOnlyDatabase
import de.jowisoftware.neo4j.content.{ NodeCompanion, IndexAccess }
import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import de.jowisoftware.neo4j.content.IndexedNodeInfo

object Keyword extends IndexedNodeCompanion[Keyword] with IndexAccess[Keyword] {
  def apply = new Keyword
  val indexInfo = IndexedNodeInfo("keyword")

  private[model] def find(db: ReadOnlyDatabase, name: String) =
    findInIndex(db, "name", IndexAccess.mask(name), this)
}

class Keyword extends MiningNode with HasIndexedName {
  def version() = 0
  def updateFrom(oldVersion: Int) = {}
}