package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.nodes.helper.HasChildWithName
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.neo4j.content.NodeCompanion
import helper.{ MiningNode, HasChildWithName, EmptyNode }
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.ContainsFiles

object CommitRepositoryRepository extends NodeCompanion[CommitRepositoryRepository] {
  def apply = new CommitRepositoryRepository
}

class CommitRepositoryRepository extends MiningNode with EmptyNode with HasChildWithName[CommitRepository] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, CommitRepository)

  def children = children(Contains, CommitRepository)
}
