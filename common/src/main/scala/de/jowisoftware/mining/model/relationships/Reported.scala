package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object Reported extends RelationshipCompanion[Reported] {
  def apply = new Reported

  val relationType = RelTypes.reported

  type sourceType = Person
  type sinkType = Ticket
}

class Reported extends EmptyRelationship {
  val companion = Reported
}