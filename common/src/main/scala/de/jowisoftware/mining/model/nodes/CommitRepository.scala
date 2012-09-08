package de.jowisoftware.mining.model.nodes

import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.{ ContainsFiles, Contains }
import de.jowisoftware.neo4j.content.NodeCompanion
import helper.{ MiningNode, HasName, EmptyNode }
import grizzled.slf4j.Logging

object CommitRepository extends NodeCompanion[CommitRepository] {
  def apply = new CommitRepository
}

class CommitRepository extends MiningNode with HasName with EmptyNode with Logging {
  def obtainCommit(id: String): Commit = {
    val uid = name()+"-"+id
    Commit.find(readableDb, uid) match {
      case Some(commit) => commit
      case None =>
        val commit = writableDb.createNode(Commit)
        commit.commitId(id)
        commit.uid(uid)
        this.add(commit, Contains)
        commit
    }
  }

  def findCommits(id: String) =
    if (supportsAbbrev())
      Commit.findAbbrev(readableDb, name()+"-"+id).toList
    else
      Commit.find(readableDb, name()+"-"+id).toList

  def findSingleCommit(id: String) =
    if (supportsAbbrev())
      Commit.findAbbrev(readableDb, name()+"-"+id).toList match {
        case Nil => None
        case commit :: Nil => Option(commit)
        case commit :: tail if tail.length < 5 =>
          warn("Commit with id '"+id+"' is not unique, returning first matching commit")
          Option(commit)
        case commit :: tail =>
          warn("Commit with id '"+id+"' is not unique and has "+(tail.length + 1)+
            " results, ignoring this reference")
          None
      }
    else
      Commit.find(readableDb, name()+"-"+id)

  def commits =
    for {
      potentialCommit <- neighbors(Direction.OUTGOING, Seq(Contains.relationType))
      if (potentialCommit.isInstanceOf[Commit])
      commit = potentialCommit.asInstanceOf[Commit]
    } yield commit

  lazy val files = getOrCreate(Direction.OUTGOING, ContainsFiles, FileRepository)

  lazy val supportsAbbrev = booleanProperty("supportsAbbrev", false)
}