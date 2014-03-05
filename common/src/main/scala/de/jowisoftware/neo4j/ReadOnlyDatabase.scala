package de.jowisoftware.neo4j

import org.neo4j.graphdb.{ GraphDatabaseService, Label, Node => NeoNode }
import de.jowisoftware.neo4j.content.{ Node, NodeCompanion }
import org.neo4j.graphdb.ResourceIterable

trait ReadOnlyDatabase {
  /**
    * Public only for testing
    */
  def service: GraphDatabaseService

  def getNode[A <: Node](id: Long, companion: NodeCompanion[A]): A
  def getUnknownNode(id: Long): Node

  def inTransaction[S](body: DBWithTransaction => S): S
}