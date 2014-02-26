package de.jowisoftware.mining.model.nodes

import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.{ Contains, ContainsFile }
import de.jowisoftware.neo4j.content.{ IndexedNodeCompanion, IndexedNodeInfo, NodeCompanion }
import grizzled.slf4j.Logging
import helper.{ EmptyNode, HasName, MiningNode }
import scala.collection.JavaConversions._
import de.jowisoftware.neo4j.content.Node

object CommitRepository extends IndexedNodeCompanion[CommitRepository] {
  def apply = new CommitRepository
  val indexInfo = IndexedNodeInfo(IndexedNodeInfo.Labels.commitRepository)
}

class CommitRepository extends MiningNode with HasName with Logging {
  val version = 2

  def updateFrom(version: Int) = {
    if (version < 2) {
      val toDeleteRel = content.getRelationships(Direction.OUTGOING, ContainsFile).iterator().next()
      val toDeleteNode = toDeleteRel.getEndNode()
      toDeleteNode.getRelationships(Direction.OUTGOING) foreach { rel =>
        val file = Node.wrapNeoNode(rel.getEndNode(), writableDb, File)
        add(file, ContainsFile)
        rel.delete()
      }
      toDeleteRel.delete()
      toDeleteNode.delete()
    }
  }

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

  def obtainFile(fileName: String): File = {
    File.find(readableDb, name(), fileName) match {
      case Some(file) => file
      case None =>
        val file = writableDb.createNode(File)
        file.name(fileName)
        file.uid(File.uid(name(), fileName))
        this.add(file, ContainsFile)
        file
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

  lazy val files = getOrCreate(Direction.OUTGOING, ContainsFile, File)

  lazy val supportsAbbrev = booleanProperty("supportsAbbrev", false)
}