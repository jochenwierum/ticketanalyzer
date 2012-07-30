package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes.helper._
import de.jowisoftware.neo4j.content._
import helper._

object Links extends RelationshipCompanion[Links] {
  def apply = new Links

  val relationType = RelTypes.links

  type sourceType = MiningNode
  type sinkType = MiningNode
}

class Links extends Relationship {
  val companion = Links

  val version = 1
  def updateFrom(oldVersion: Int) = {}

  lazy val linkType = stringProperty("linkType")
}