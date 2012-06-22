package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object HasResolution extends RelationshipCompanion[HasResolution] {
  def apply = new HasResolution

  val relationType = RelTypes.hasResolution

  type sourceType = Ticket
  type sinkType = Resolution
}

class HasResolution extends EmptyRelationship {
  val companion = HasResolution
}