package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Reproducability extends NodeCompanion[Reproducability] {
  def apply = new Reproducability
}

class Reproducability extends MiningNode with EmptyNode with HasName
