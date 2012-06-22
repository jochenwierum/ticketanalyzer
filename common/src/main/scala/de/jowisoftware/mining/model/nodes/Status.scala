package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Status extends NodeCompanion[Status] {
  def apply = new Status
}

class Status extends MiningNode with HasName with EmptyNode