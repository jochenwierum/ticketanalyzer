package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object HasStatus extends RelationshipCompanion[HasStatus] {
  def apply = new HasStatus

  val relationType = RelTypes.hasStatus

  type sourceType = Ticket
  type sinkType = Status
}

class HasStatus extends EmptyRelationship {
  val companion = HasStatus
}
