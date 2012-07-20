package de.jowisoftware.neo4j.content.index

import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.PropertyContainer

object NodeIndexCreator extends IndexCreator {
  def create(service: AbstractGraphDatabase, node: PropertyContainer, indexName: String, name: String): Index =
    new DefaultIndex(service.index.forNodes(indexName), node.asInstanceOf[Node], name)
}
