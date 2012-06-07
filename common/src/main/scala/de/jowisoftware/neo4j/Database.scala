package de.jowisoftware.neo4j

import org.neo4j.kernel.AbstractGraphDatabase
import content.Node
import content.NodeCompanion
import database.EmbeddedDatabase
import de.jowisoftware.util.FileUtils
import de.jowisoftware.neo4j.database.EmbeddedDatabaseWithConsole

trait Database[T <: Node] {
  def shutdown
  def inTransaction[S](body: DBWithTransaction[T] => S): S
  def deleteContent

  def service: AbstractGraphDatabase
}

object Database {
  def apply[T <: Node](path: String, rootNode: NodeCompanion[T]) = new EmbeddedDatabaseWithConsole(path, rootNode)
  def drop(path: String) = FileUtils.delTree(path)
}