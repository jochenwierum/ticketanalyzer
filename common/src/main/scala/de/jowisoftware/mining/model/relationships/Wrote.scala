package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object Wrote extends RelationshipCompanion[Wrote] {
  def apply = new Wrote

  val relationType = RelTypes.wrote

  type sourceType = TicketComment
  type sinkType = Person
}

class Wrote extends EmptyRelationship {
  val companion = Wrote
}