package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object StatusRepository extends NodeCompanion[StatusRepository] {
  def apply = new StatusRepository
}

class StatusRepository extends MiningNode with EmptyNode with HasChildWithName[Status] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Status)

  def children = children(Contains, Status)
}
