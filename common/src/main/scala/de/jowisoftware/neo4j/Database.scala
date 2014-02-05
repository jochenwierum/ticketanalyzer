package de.jowisoftware.neo4j

import content.Node
import org.neo4j.graphdb.GraphDatabaseService

trait Database[T <: Node] extends ReadOnlyDatabase[T] {
  def shutdown
  def inTransaction[S](body: DBWithTransaction[T] => S): S
  def startTransaction: DBWithTransaction[T]
  def deleteContent

  def service: GraphDatabaseService
}