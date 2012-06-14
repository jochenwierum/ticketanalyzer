package de.jowisoftware.neo4j

import de.jowisoftware.neo4j.database.EmbeddedDatabaseWithConsole
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.util.FileUtils
import de.jowisoftware.mining.model.Node
import java.io.File

object EmbeddedDatabase {
  def apply[T <: Node](path: File, rootNode: NodeCompanion[T]) = new EmbeddedDatabaseWithConsole(path, rootNode)
  def drop(path: File) = FileUtils.delTree(path)
}