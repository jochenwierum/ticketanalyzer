package de.jowisoftware.mining.model

import de.jowisoftware.neo4j.content.NodeCompanion
import helper._

object VersionRepository extends NodeCompanion[VersionRepository] {
  def apply = new VersionRepository
}

class VersionRepository extends MiningNode with EmptyNode with HasChildWithName[Version] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Version)
}