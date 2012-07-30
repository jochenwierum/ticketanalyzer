package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object TagRepository extends NodeCompanion[TagRepository] {
  def apply = new TagRepository
}

class TagRepository extends MiningNode with EmptyNode with HasChildWithName[Tag] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Tag)

  def children = children(Contains, Tag)
}
