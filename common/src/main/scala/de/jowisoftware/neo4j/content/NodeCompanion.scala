package de.jowisoftware.neo4j.content

trait NodeCompanion[+T <: Node] {
  def apply() : T
}
