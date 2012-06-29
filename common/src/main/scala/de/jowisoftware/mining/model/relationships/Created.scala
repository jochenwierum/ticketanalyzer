package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object Created extends RelationshipCompanion[Created] {
  def apply = new Created

  val relationType = RelTypes.created

  type sourceType = TicketComment
  type sinkType = Person
}

class Created extends EmptyRelationship {
  val companion = Created
}