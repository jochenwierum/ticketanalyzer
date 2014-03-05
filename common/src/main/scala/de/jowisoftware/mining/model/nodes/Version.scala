package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Version extends IndexedNodeCompanion[Version] {
  def apply = new Version
  protected val primaryProperty = HasName.properties.name
}

class Version extends MiningNode with HasName with EmptyNode