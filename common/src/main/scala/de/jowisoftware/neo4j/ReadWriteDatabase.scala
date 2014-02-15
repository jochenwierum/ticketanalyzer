package de.jowisoftware.neo4j

import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.neo4j.content.Node

trait ReadWriteDatabase extends ReadOnlyDatabase {
  def createNode[A <: Node](companion: NodeCompanion[A]): A
  def collections: DatabaseCollection
}