package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object SponsoredBy extends RelationshipCompanion[SponsoredBy] {
  def apply = new SponsoredBy

  val relationType = RelTypes.sponsoredBy

  type sourceType = Ticket
  type sinkType = Person
}

class SponsoredBy extends EmptyRelationship {
  val companion = SponsoredBy
}