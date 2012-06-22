package de.jowisoftware.mining.model

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Milestone extends NodeCompanion[Milestone] {
  def apply = new Milestone
}

class Milestone extends MiningNode with HasName with EmptyNode