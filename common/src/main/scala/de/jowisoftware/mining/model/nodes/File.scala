package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object File extends NodeCompanion[File] with IndexAccess[File] {
  def apply = new File

  private[model] def find(db: ReadOnlyDatabase, repository: String, name: String): Option[File] =
    findInIndex(db, "uid", uid(repository, name), this)

  def uid(repository: String, name: String): String =
    repository+"-"+name
}

class File extends MiningNode with HasName {
  def updateFrom(version: Int) {}
  val version = 1

  lazy val uid = stringProperty("uid", "", true)
}