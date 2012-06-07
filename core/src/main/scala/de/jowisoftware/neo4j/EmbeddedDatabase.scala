package de.jowisoftware.neo4j

import de.jowisoftware.neo4j.database.EmbeddedDatabaseWithConsole
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.util.FileUtils
import de.jowisoftware.mining.model.Node

object EmbeddedDatabase {
  def apply[T <: Node](path: String, rootNode: NodeCompanion[T]) = new EmbeddedDatabaseWithConsole(path, rootNode)
  def drop(path: String) = FileUtils.delTree(path)
}