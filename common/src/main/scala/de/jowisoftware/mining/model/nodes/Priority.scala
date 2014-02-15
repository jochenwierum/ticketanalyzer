package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Priority extends IndexedNodeCompanion[Priority] {
  def apply = new Priority
  val indexInfo = IndexedNodeInfo("priority")
}

class Priority extends MiningNode with EmptyNode with HasName