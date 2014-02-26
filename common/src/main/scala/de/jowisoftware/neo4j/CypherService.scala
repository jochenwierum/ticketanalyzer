package de.jowisoftware.neo4j

import org.neo4j.cypher.ExecutionResult

trait CypherService {
  def execute(query: String): ExecutionResult
  def execute(query: String, variables: Map[String, Any]): ExecutionResult
}