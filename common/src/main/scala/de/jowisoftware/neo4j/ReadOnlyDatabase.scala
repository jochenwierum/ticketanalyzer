package de.jowisoftware.neo4j

import de.jowisoftware.neo4j.content.Node
import org.neo4j.kernel.AbstractGraphDatabase
import de.jowisoftware.neo4j.content.NodeCompanion
import org.neo4j.graphdb.GraphDatabaseService

trait ReadOnlyDatabase[T <: Node] {
  def rootNode: T

  /**
    * Public only for testing
    */
  def service: GraphDatabaseService

  def getNode[T <: Node](id: Long, companion: NodeCompanion[T]): T
  def getUnknownNode(id: Long): Node
}