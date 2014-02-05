package de.jowisoftware.neo4j.content.index

import org.neo4j.graphdb.PropertyContainer
import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.GraphDatabaseService

trait IndexCreator {
  def create(db: GraphDatabaseService, node: PropertyContainer, indexName: String, name: String): Index
}
