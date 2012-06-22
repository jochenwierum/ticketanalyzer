package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object ReportedBy extends RelationshipCompanion[ReportedBy] {
  def apply = new ReportedBy

  val relationType = RelTypes.reportedBy

  type sourceType = Ticket
  type sinkType = Person
}

class ReportedBy extends EmptyRelationship {
  val companion = ReportedBy
}