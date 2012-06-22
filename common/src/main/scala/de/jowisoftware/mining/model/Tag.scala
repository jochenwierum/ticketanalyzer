package de.jowisoftware.mining.model

import de.jowisoftware.neo4j.content.NodeCompanion
import helper._

object Tag extends NodeCompanion[Tag] {
  def apply = new Tag
}

class Tag extends MiningNode with EmptyNode with HasName