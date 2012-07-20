package de.jowisoftware.neo4j.content.index

import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.PropertyContainer
import org.neo4j.graphdb.Relationship

object RelationshipIndexCreator extends IndexCreator {
  def create(service: AbstractGraphDatabase, relationship: PropertyContainer, indexName: String, name: String): Index =
    new DefaultIndex(service.index.forRelationships(indexName), relationship.asInstanceOf[Relationship], name)
}
