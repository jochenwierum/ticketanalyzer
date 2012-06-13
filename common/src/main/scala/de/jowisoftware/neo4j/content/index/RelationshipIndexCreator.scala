package de.jowisoftware.neo4j.content.index

import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.PropertyContainer

object RelationshipIndexCreator extends IndexCreator {
  def create(db: AbstractGraphDatabase, relationship: PropertyContainer, name: String): Index =
      new DefaultIndex(db.index.forRelationships(name), relationship.asInstanceOf[Relationship], name)
}
