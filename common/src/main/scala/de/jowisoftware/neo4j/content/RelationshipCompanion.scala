package de.jowisoftware.neo4j.content

import org.neo4j.graphdb.RelationshipType

trait RelationshipCompanion[+T <: Relationship] {
  val relationType: RelationshipType

  def apply(): T
  protected[neo4j] type sourceType <: Node
  protected[neo4j] type sinkType <: Node
}
