package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object ChildOf extends RelationshipCompanion[ChildOf] {
  def apply = new ChildOf

  val relationType = RelTypes.childOf

  type sourceType = Commit
  type sinkType = Commit
}

class ChildOf extends EmptyRelationship {
  val companion = ChildOf
}