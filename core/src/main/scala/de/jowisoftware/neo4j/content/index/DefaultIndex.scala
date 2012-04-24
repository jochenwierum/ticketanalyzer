package de.jowisoftware.neo4j.content.index

import org.neo4j.graphdb.PropertyContainer

import org.neo4j.graphdb.index.{Index => NeoIndex}

class DefaultIndex[T <: PropertyContainer](index: NeoIndex[T], parent: T, name: String) extends Index {
  def index(value: Any) = {
    remove()
    index.add(parent, name, value)
  }

  def remove() =
    index.remove(parent, name)
}