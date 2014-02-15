package de.jowisoftware.neo4j

import org.neo4j.graphdb.GraphDatabaseService

import de.jowisoftware.neo4j.content.{ Node, NodeCompanion }

trait ReadOnlyDatabase {
  /**
    * Public only for testing
    */
  def service: GraphDatabaseService

  def getNode[A <: Node](id: Long, companion: NodeCompanion[A]): A
  def getUnknownNode(id: Long): Node
}