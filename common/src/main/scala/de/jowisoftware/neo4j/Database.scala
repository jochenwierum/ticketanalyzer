package de.jowisoftware.neo4j

import org.neo4j.graphdb.GraphDatabaseService

trait Database extends ReadOnlyDatabaseWithTransaction {
  def shutdown()
  def startTransaction: DBWithTransaction
  def deleteContent()

  def service: GraphDatabaseService
}
