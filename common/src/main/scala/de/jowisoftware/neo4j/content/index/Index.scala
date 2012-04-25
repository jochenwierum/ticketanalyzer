package de.jowisoftware.neo4j.content.index

private[content] trait Index {
  def index(value: Any)
  def remove()
}