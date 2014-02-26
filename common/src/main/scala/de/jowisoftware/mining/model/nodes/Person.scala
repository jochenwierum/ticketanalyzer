package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Person extends IndexedNodeCompanion[Person] {
  def apply = new Person
  val indexInfo = IndexedNodeInfo(IndexedNodeInfo.Labels.person)
}

class Person extends MiningNode with HasName with EmptyNode