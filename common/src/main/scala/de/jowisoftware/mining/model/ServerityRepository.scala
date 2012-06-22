package de.jowisoftware.mining.model

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object SeverityRepository extends NodeCompanion[SeverityRepository] {
  def apply = new SeverityRepository
}

class SeverityRepository extends MiningNode with EmptyNode with HasChildWithName[Severity] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Severity)
}
