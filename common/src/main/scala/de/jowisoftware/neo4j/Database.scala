package de.jowisoftware.neo4j

import content.Node
import org.neo4j.graphdb.GraphDatabaseService

trait Database extends ReadOnlyDatabase {
  def shutdown
  def inTransaction[S](body: DBWithTransaction => S): S
  def startTransaction: DBWithTransaction
  def deleteContent

  def service: GraphDatabaseService
}