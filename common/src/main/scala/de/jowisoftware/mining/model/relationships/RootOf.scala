package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object RootOf extends RelationshipCompanion[RootOf] {
  def apply = new RootOf

  val relationType = RelTypes.rootOf

  type sourceType = Ticket
  type sinkType = Ticket
}

class RootOf extends EmptyRelationship {
  val companion = RootOf
}
