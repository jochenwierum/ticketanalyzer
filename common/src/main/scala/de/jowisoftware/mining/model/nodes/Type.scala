package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Type extends NodeCompanion[Type] {
  def apply = new Type
}

class Type extends MiningNode with HasName with EmptyNode