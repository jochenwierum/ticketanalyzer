package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Person extends NodeCompanion[Person] {
  def apply = new Person
}

class Person extends MiningNode with HasName with EmptyNode