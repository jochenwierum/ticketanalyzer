package de.jowisoftware.mining.model

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Commit extends NodeCompanion[Commit] {
  def apply = new Commit

  private[model] def find(db: DBWithTransaction[RootNode], repository: String, id: String) = {
    val uid = repository+"-"+id
    val result = getIndex(db).query("uid", uid).getSingle

    if (result == null)
      None
    else
      Some(Node.wrapNeoNode(result, db)(this))
  }
}

class Commit extends MiningNode {
  val version = 1
  def updateFrom(version: Int) = {}

  lazy val commitId = stringProperty("id")
  lazy val uid = stringProperty("uid", "", true)
  lazy val message = stringProperty("message")
  lazy val date = dateProperty("date")
}