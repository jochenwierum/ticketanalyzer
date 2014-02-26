package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Reproducability extends IndexedNodeCompanion[Reproducability] {
  def apply = new Reproducability
  val indexInfo = IndexedNodeInfo(IndexedNodeInfo.Labels.reproducability)
}

class Reproducability extends MiningNode with EmptyNode with HasName
