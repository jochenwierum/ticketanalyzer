package de.jowisoftware.neo4j.database

import de.jowisoftware.neo4j.CypherService
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService

class Cypher private[database] (db: GraphDatabaseService) extends CypherService {
  private val engine = new ExecutionEngine(db)

  def execute(query: String) = engine.execute(query)
  def execute(query: String, variables: Map[String, Any]) = engine.execute(query, variables)
}
