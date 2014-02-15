package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Type extends IndexedNodeCompanion[Type] {
  def apply = new Type
  val indexInfo = IndexedNodeInfo("type")
}

class Type extends MiningNode with HasName with EmptyNode