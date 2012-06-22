package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content.NodeCompanion
import helper._

object Component extends NodeCompanion[Component] {
  def apply = new Component
}

class Component extends MiningNode with HasName with EmptyNode