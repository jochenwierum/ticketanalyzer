package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object ReproducabilityRepository extends NodeCompanion[ReproducabilityRepository] {
  def apply = new ReproducabilityRepository
}

class ReproducabilityRepository extends MiningNode with EmptyNode with HasChildWithName[Reproducability] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Reproducability)
}
