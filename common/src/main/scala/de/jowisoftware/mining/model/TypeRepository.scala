package de.jowisoftware.mining.model

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object TypeRepository extends NodeCompanion[TypeRepository] {
  def apply = new TypeRepository
}

class TypeRepository extends MiningNode with EmptyNode with HasChildWithName[Type] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Type)
}