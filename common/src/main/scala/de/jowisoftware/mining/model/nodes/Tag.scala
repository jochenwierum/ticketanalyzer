package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content.NodeCompanion
import helper._
import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import de.jowisoftware.neo4j.content.IndexedNodeInfo

object Tag extends IndexedNodeCompanion[Tag] {
  def apply = new Tag
  val indexInfo = IndexedNodeInfo(IndexedNodeInfo.Labels.tag)
}

class Tag extends MiningNode with EmptyNode with HasName