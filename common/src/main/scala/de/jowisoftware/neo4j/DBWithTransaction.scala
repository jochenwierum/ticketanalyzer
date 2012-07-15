package de.jowisoftware.neo4j

import content.{ Node, NodeCompanion }
import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.{ Node => NeoNode, Relationship => NeoRelationship }

/**
  * A transaction which allows access to the database.
  */
trait DBWithTransaction[T <: Node] {
  /**
    * Mark this transaction as successful.
    * After calling this method, no other methods of this object must be called.
    */
  def success

  /**
    * Mark this transaction as failed.
    * After calling this method, no other methods of this object must be called.
    */
  def failure

  val rootNode: T

  /**
    * Public only for testing
    */
  def service: AbstractGraphDatabase

  def createNode[T <: Node](companion: NodeCompanion[T]): T
  def getNode[T <: Node](id: Long, companion: NodeCompanion[T]): T
  def getUnknownNode(id: Long): Node
}
