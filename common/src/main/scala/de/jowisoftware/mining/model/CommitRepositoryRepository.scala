package de.jowisoftware.mining.model

import de.jowisoftware.neo4j.content.NodeCompanion
import helper._

object CommitRepositoryRepository extends NodeCompanion[CommitRepositoryRepository] {
  def apply = new CommitRepositoryRepository
}

class CommitRepositoryRepository extends MiningNode with EmptyNode with HasChildWithName[CommitRepository] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, CommitRepository)
}
