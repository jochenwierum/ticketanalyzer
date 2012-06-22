package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object PriorityRepository extends NodeCompanion[PriorityRepository] {
  def apply = new PriorityRepository
}

class PriorityRepository extends MiningNode with EmptyNode with HasChildWithName[Priority] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Priority)
}