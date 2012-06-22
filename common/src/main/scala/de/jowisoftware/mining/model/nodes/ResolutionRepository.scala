package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object ResolutionRepository extends NodeCompanion[ResolutionRepository] {
  def apply = new ResolutionRepository
}

class ResolutionRepository extends MiningNode with EmptyNode with HasChildWithName[Resolution] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Resolution)
}