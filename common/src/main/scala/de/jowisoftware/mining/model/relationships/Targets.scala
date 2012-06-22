package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object Targets extends RelationshipCompanion[Targets] {
  def apply = new Targets

  val relationType = RelTypes.targets

  type sourceType = Ticket
  type sinkType = Version
}

class Targets extends EmptyRelationship {
  val companion = Targets
}