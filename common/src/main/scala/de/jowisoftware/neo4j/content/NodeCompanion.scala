package de.jowisoftware.neo4j.content

trait NodeCompanion[A <: Node] {
  def apply(): A
}
