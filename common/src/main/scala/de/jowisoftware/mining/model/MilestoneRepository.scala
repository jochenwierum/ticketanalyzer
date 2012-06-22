package de.jowisoftware.mining.model

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object MilestoneRepository extends NodeCompanion[MilestoneRepository] {
  def apply = new MilestoneRepository
}

class MilestoneRepository extends MiningNode with EmptyNode with HasChildWithName[Milestone] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Milestone)
}