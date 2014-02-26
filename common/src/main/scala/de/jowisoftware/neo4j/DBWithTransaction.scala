package de.jowisoftware.neo4j

import content.{ Node, NodeCompanion }
import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.{ Node => NeoNode, Relationship => NeoRelationship }
import de.jowisoftware.neo4j.content.NodeCompanion

/**
  * A transaction which allows access to the database.
  */
trait DBWithTransaction extends ReadWriteDatabase {
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

  def rootNode[A <: Node](companion: NodeCompanion[A]): A

  def cypher(query: String) = cypherService.execute(query)
  def cypher(query: String, variables: Map[String, Any]) = cypherService.execute(query, variables)
}
