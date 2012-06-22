package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object HasPriority extends RelationshipCompanion[HasPriority] {
  def apply = new HasPriority

  val relationType = RelTypes.hasPriority

  type sourceType = Ticket
  type sinkType = Priority
}

class HasPriority extends EmptyRelationship {
  val companion = HasPriority
}
