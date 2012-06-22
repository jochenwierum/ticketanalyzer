package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object File extends NodeCompanion[File] {
  def apply = new File

  private[model] def find(db: DBWithTransaction[RootNode], repository: String, name: String) =
    findInIndex(db, "uid", repository+"-"+name)
}

class File extends MiningNode with HasName {
  def updateFrom(version: Int) {}
  val version = 1

  lazy val uid = stringProperty("uid", "", true)
}