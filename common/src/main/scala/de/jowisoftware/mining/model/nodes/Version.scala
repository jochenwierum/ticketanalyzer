package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Version extends NodeCompanion[Version] {
  def apply = new Version
}

class Version extends MiningNode with HasName with EmptyNode