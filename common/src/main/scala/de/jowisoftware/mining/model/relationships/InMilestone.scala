package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object InMilestone extends RelationshipCompanion[InMilestone] {
  def apply = new InMilestone

  val relationType = RelTypes.inMilestone

  type sourceType = Ticket
  type sinkType = Milestone
}

class InMilestone extends EmptyRelationship {
  val companion = InMilestone
}
