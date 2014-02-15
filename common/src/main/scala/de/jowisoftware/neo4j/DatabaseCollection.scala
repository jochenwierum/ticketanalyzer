package de.jowisoftware.neo4j

import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import de.jowisoftware.neo4j.content.Node
import org.neo4j.graphdb.ResourceIterable

trait DatabaseCollection {
  def find[A <: Node](companion: IndexedNodeCompanion[A], name: String): Option[A]
  def findAll[A <: Node](companion: IndexedNodeCompanion[A]): Iterator[A]
  def findOrCreate[A <: Node](companion: IndexedNodeCompanion[A], name: String): A
}