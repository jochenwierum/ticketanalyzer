package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object HasTag extends RelationshipCompanion[HasTag] {
  def apply = new HasTag

  val relationType = RelTypes.hasTag

  type sourceType = Ticket
  type sinkType = Tag
}

class HasTag extends EmptyRelationship {
  val companion = HasTag
}