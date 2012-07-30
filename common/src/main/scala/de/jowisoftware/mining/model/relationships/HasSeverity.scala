package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object HasSeverity extends RelationshipCompanion[HasSeverity] {
  def apply = new HasSeverity

  val relationType = RelTypes.hasSeverity

  type sourceType = Ticket
  type sinkType = Severity
}

class HasSeverity extends EmptyRelationship {
  val companion = HasSeverity
}