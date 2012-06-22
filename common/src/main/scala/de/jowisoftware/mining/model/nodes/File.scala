package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object File extends NodeCompanion[File] {
  def apply = new File

  def find(db: DBWithTransaction[RootNode], repository: String, name: String) = {
    val uid = repository+"-"+name
    val result = getIndex(db).query("uid", uid).getSingle

    if (result == null)
      None
    else
      Some(Node.wrapNeoNode(result, db)(this))
  }
}

class File extends MiningNode with HasName {
  def updateFrom(version: Int) {}
  val version = 1

  lazy val uid = stringProperty("uid", "", true)
}