package de.jowisoftware.mining.model.relationships

import de.jowisoftware.neo4j.content.{RelationshipCompanion, Node}
import helper.{RelTypes, EmptyRelationship}

object Contains extends RelationshipCompanion[Contains] {
  def apply = new Contains

  val relationType = RelTypes.contains

  type sourceType = Node
  type sinkType = Node
}

class Contains extends EmptyRelationship {
  val companion = Contains
}
