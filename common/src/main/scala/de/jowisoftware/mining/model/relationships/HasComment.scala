package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object HasComment extends RelationshipCompanion[HasComment] {
  def apply = new HasComment

  val relationType = RelTypes.hasComment

  type sourceType = Ticket
  type sinkType = TicketComment
}

class HasComment extends EmptyRelationship {
  val companion = HasComment
}
