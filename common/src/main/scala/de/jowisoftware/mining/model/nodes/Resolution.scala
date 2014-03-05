package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Resolution extends IndexedNodeCompanion[Resolution] {
  def apply = new Resolution
  protected val primaryProperty = HasName.properties.name
}

class Resolution extends MiningNode with EmptyNode with HasName