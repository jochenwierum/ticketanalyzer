package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object InComponent extends RelationshipCompanion[InComponent] {
  def apply = new InComponent

  val relationType = RelTypes.inComponent

  type sourceType = Ticket
  type sinkType = Component
}

class InComponent extends EmptyRelationship {
  val companion = InComponent
}