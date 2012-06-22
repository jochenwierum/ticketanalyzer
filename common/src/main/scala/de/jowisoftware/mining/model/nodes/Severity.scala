package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Severity extends NodeCompanion[Severity] {
  def apply = new Severity
}

class Severity extends MiningNode with EmptyNode with HasName
