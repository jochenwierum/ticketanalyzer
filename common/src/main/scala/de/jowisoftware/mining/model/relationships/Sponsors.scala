package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object Sponsors extends RelationshipCompanion[Sponsors] {
  def apply = new Sponsors

  val relationType = RelTypes.sponsors

  type sourceType = Ticket
  type sinkType = Person
}

class Sponsors extends EmptyRelationship {
  val companion = Sponsors
}