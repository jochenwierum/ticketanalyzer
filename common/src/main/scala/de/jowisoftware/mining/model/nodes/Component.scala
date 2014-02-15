package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content.NodeCompanion
import helper._
import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import de.jowisoftware.neo4j.content.IndexedNodeInfo

object Component extends IndexedNodeCompanion[Component] {
  def apply = new Component
  val indexInfo = IndexedNodeInfo("component")
}

class Component extends MiningNode with HasName with EmptyNode