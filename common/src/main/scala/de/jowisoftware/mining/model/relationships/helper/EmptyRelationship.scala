package de.jowisoftware.mining.model.relationships.helper

import de.jowisoftware.neo4j.content.Relationship

trait EmptyRelationship extends Relationship {
  val version = 0
  def updateFrom(version: Int) = {}
}