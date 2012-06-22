package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object HasReproducability extends RelationshipCompanion[HasReproducability] {
  def apply = new HasReproducability

  val relationType = RelTypes.hasReproducability

  type sourceType = Ticket
  type sinkType = Reproducability
}

class HasReproducability extends EmptyRelationship {
  val companion = HasReproducability
}