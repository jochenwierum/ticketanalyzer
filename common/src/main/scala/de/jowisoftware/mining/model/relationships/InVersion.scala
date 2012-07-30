package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object InVersion extends RelationshipCompanion[InVersion] {
  def apply = new InVersion

  val relationType = RelTypes.inVersion

  type sourceType = Ticket
  type sinkType = Version
}

class InVersion extends EmptyRelationship {
  val companion = InVersion
}