package de.jowisoftware.mining.model.nodes

import org.neo4j.graphdb.Direction

import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.mining.model.relationships.ContainsFiles

import helper.{MiningNode, HasName, EmptyNode}

object CommitRepository extends NodeCompanion[CommitRepository] {
  def apply = new CommitRepository
}

class CommitRepository extends MiningNode with HasName with EmptyNode {
  def createCommit(): Commit = db.createNode(Commit)

  def findCommit(id: String) = Commit.find(db, name(), id)
  lazy val files = getOrCreate(Direction.OUTGOING, ContainsFiles)(FileRepository)
}