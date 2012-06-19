package de.jowisoftware.neo4j.content.index

import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.PropertyContainer
import de.jowisoftware.neo4j.DBWithTransaction

object RelationshipIndexCreator extends IndexCreator {
  def create(db: DBWithTransaction[_], relationship: PropertyContainer, indexName: String, name: String): Index =
    new DefaultIndex(db.service.index.forRelationships(indexName), relationship.asInstanceOf[Relationship], name)
}
