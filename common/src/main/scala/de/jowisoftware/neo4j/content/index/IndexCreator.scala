package de.jowisoftware.neo4j.content.index

import org.neo4j.graphdb.PropertyContainer
import org.neo4j.kernel.AbstractGraphDatabase

trait IndexCreator {
  def create(db: AbstractGraphDatabase, node: PropertyContainer, indexName: String, name: String): Index
}
