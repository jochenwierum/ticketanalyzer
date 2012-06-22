package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object HasType extends RelationshipCompanion[HasType] {
  def apply = new HasType

  val relationType = RelTypes.hasType

  type sourceType = Ticket
  type sinkType = Type
}

class HasType extends EmptyRelationship {
  val companion = HasType
}
