package de.jowisoftware.neo4j

import de.jowisoftware.neo4j.content.{Node, NodeCompanion}
import grizzled.slf4j.Logging
import org.neo4j.graphdb.{Label, ResourceIterable, Node => NeoNode}

/**
  * A transaction which allows access to the database.
  */
trait DBWithTransaction extends ReadOnlyDatabase with Logging {
  protected val cypherService: CypherService

  /**
    * Mark this transaction as successful.
    * After calling this method, no other methods of this object must be called.
    */
  def success()

  /**
    * Mark this transaction as failed.
    * After calling this method, no other methods of this object must be called.
    */
  def failure()

  def createNode[A <: Node](companion: NodeCompanion[A]): A

  def rootNode[A <: Node](companion: NodeCompanion[A]): A

  def cypher(query: String) = cypherService.execute(query)
  def cypher(query: String, variables: Map[String, Any]) = cypherService.execute(query, variables)

  def findNodesByLabelAndProperty(label: Label, key: String, value: AnyRef): ResourceIterable[NeoNode]
}
