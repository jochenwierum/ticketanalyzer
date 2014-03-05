package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Reproducability extends IndexedNodeCompanion[Reproducability] {
  def apply = new Reproducability
  protected val primaryProperty = HasName.properties.name
}

class Reproducability extends MiningNode with EmptyNode with HasName
