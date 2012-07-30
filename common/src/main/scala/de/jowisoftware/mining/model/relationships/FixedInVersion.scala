package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object FixedInVersion extends RelationshipCompanion[FixedInVersion] {
  def apply = new FixedInVersion

  val relationType = RelTypes.fixedInVersion

  type sourceType = Ticket
  type sinkType = Version
}

class FixedInVersion extends EmptyRelationship {
  val companion = FixedInVersion
}
