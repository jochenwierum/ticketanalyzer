package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object Owns extends RelationshipCompanion[Owns] {
  def apply = new Owns

  val relationType = RelTypes.owns

  type sourceType = Person
  type sinkType = Ticket
}

class Owns extends EmptyRelationship {
  val companion = Owns
}