package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.nodes.helper.{ HasName, MiningNode }
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.{ IndexedNodeCompanion, RegexIndexAccess }

object Keyword extends IndexedNodeCompanion[Keyword] {
  def apply = new Keyword
  protected val primaryProperty = HasName.properties.name
}

class Keyword extends MiningNode with HasName {
  def version() = 0
  def updateFrom(oldVersion: Int) = {}
}