package de.jowisoftware.neo4j.content.index

object NullIndex extends Index {
  def index(value: Any) {}
  def remove() {}
}