package de.jowisoftware.mining.model.nodes

import org.neo4j.graphdb.Direction

import de.jowisoftware.mining.model.relationships.{ ContainsFiles, Contains }
import de.jowisoftware.neo4j.content.NodeCompanion
import helper.{ MiningNode, HasName, EmptyNode }

object CommitRepository extends NodeCompanion[CommitRepository] {
  def apply = new CommitRepository
}

class CommitRepository extends MiningNode with HasName with EmptyNode {
  def obtainCommit(id: String): Commit = {
    val uid = name()+"-"+id
    Commit.find(db, uid) match {
      case Some(commit) => commit
      case None =>
        val commit = db.createNode(Commit)
        commit.commitId(id)
        commit.uid(uid)
        this.add(commit, Contains)
        commit
    }
  }

  def findCommit(id: String) = Commit.find(db, name()+"-"+id)
  lazy val files = getOrCreate(Direction.OUTGOING, ContainsFiles, FileRepository)
}