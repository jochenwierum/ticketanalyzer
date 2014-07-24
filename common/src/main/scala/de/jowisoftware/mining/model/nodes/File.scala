package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.nodes.helper._
import de.jowisoftware.neo4j._
import de.jowisoftware.neo4j.content._

object File extends IndexedNodeCompanion[File] {
  def apply = new File

  protected val primaryProperty = "uid"

  private[model] def find(db: DBWithTransaction, repository: String, name: String): Option[File] =
    find(db, uid(repository, name))

  def uid(repository: String, name: String): String =
    repository+"-"+name
}

class File extends MiningNode with HasName {
  def updateFrom(version: Int) {}
  val version = 1
  val uid = stringProperty("uid", "")
}
