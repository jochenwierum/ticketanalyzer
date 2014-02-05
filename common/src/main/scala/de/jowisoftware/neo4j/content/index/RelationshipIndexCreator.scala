package de.jowisoftware.neo4j.content.index

import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.PropertyContainer
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.GraphDatabaseService

object RelationshipIndexCreator extends IndexCreator {
  def create(service: GraphDatabaseService, relationship: PropertyContainer, indexName: String, name: String): Index =
    new DefaultIndex(service.index.forRelationships(indexName), relationship.asInstanceOf[Relationship], name)
}
