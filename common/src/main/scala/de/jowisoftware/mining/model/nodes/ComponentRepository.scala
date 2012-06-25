package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.neo4j._
import helper._

object ComponentRepository extends NodeCompanion[ComponentRepository] {
  def apply = new ComponentRepository
}

class ComponentRepository extends MiningNode with EmptyNode with HasChildWithName[Component] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Component)
}