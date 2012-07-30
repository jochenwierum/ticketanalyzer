package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object FromPerson extends RelationshipCompanion[FromPerson] {
  def apply = new FromPerson

  val relationType = RelTypes.fromPerson

  type sourceType = Node
  type sinkType = Person
}

class FromPerson extends EmptyRelationship {
  val companion = FromPerson
}