package de.jowisoftware.neo4j.content.index

import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.PropertyContainer
import de.jowisoftware.neo4j.DBWithTransaction

trait IndexCreator {
  def create(db: DBWithTransaction[_], node: PropertyContainer, indexName: String, name: String): Index
}
