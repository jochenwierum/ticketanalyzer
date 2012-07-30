package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object Updates extends RelationshipCompanion[Updates] {
  def apply = new Updates

  val relationType = RelTypes.updates

  type sourceType = Ticket
  type sinkType = Ticket
}

class Updates extends EmptyRelationship {
  val companion = Updates
}
