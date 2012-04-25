package de.jowisoftware.neo4j.content.index

import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.PropertyContainer

object NodeIndexCreator extends IndexCreator {
  def create(db: AbstractGraphDatabase, node: PropertyContainer, name: String): Index =
      new DefaultIndex(db.index.forNodes(name), node.asInstanceOf[Node], name)
}