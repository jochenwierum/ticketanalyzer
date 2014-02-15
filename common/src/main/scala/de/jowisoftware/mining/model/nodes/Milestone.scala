package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Milestone extends IndexedNodeCompanion[Milestone] {
  def apply = new Milestone
  val indexInfo = IndexedNodeInfo("Milestone")
}

class Milestone extends MiningNode with HasName with EmptyNode