package de.jowisoftware.neo4j.content.index

import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.PropertyContainer

trait IndexCreator {
  def create(db: AbstractGraphDatabase, node: PropertyContainer, name: String): Index
}