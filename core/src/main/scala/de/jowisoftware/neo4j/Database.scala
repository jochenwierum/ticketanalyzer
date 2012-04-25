package de.jowisoftware.neo4j

import org.neo4j.kernel.AbstractGraphDatabase

import content.Node
import content.NodeCompanion
import database.EmbeddedDatabase
import de.jowisoftware.util.FileUtils

trait Database[T <: Node] {
  def shutdown
  def inTransaction[S](body: DBWithTransaction[T] => S): S
  def deleteContent

  private[neo4j] def service: AbstractGraphDatabase
}

object Database {
  def apply[T <: Node](path: String, rootNode: NodeCompanion[T]) = new EmbeddedDatabase(path, rootNode)
  def drop(path: String) = FileUtils.delTree(path)
}