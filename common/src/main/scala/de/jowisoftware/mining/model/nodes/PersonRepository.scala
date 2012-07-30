package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object PersonRepository extends NodeCompanion[PersonRepository] {
  def apply = new PersonRepository
}

class PersonRepository extends MiningNode with EmptyNode with HasChildWithName[Person] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Person)

  def children = children(Contains, Person)
}