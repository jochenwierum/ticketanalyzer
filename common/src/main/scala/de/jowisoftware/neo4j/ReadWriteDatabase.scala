package de.jowisoftware.neo4j

import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.neo4j.content.Node

trait ReadWriteDatabase[T <: Node] extends ReadOnlyDatabase[T] {
  def createNode[T <: Node](companion: NodeCompanion[T]): T
}