package de.jowisoftware.mining.model.nodes

import org.neo4j.graphdb.{RelationshipType, Direction}

import de.jowisoftware.mining.model.relationships.{ Contains, ContainsFiles }
import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object FileRepository extends NodeCompanion[FileRepository] {
  def apply = new FileRepository
}

class FileRepository extends MiningNode with EmptyNode {
  private lazy val parentName = getFirstNeighbor(Direction.INCOMING, ContainsFiles)(CommitRepository).get.name()

  def createFile(): File = db.createNode(File)
  def findFile(name: String) = File.find(db, parentName, name)
}