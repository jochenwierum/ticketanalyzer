package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.ChildOf

object Commit extends NodeCompanion[Commit] {
  def apply = new Commit

  private[model] def find(db: DBWithTransaction[RootNode], uid: String) =
    findInIndex(db, "uid", uid)
}

class Commit extends MiningNode {
  val version = 1
  def updateFrom(version: Int) = {}

  lazy val commitId = stringProperty("id")
  lazy val uid = stringProperty("uid", "", true)
  lazy val message = stringProperty("message")
  lazy val date = dateProperty("date")

  lazy val rank = intProperty("rank", 0)

  def children =
    neighbors(Direction.INCOMING, Seq(ChildOf.relationType)) map (_.asInstanceOf[Commit])

  def parents =
    neighbors(Direction.OUTGOING, Seq(ChildOf.relationType)) map (_.asInstanceOf[Commit])
}