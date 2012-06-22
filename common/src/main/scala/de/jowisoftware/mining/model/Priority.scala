package de.jowisoftware.mining.model

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Priority extends NodeCompanion[Priority] {
  def apply = new Priority
}

class Priority extends MiningNode with EmptyNode with HasName