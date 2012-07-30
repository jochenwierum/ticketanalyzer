package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object References extends RelationshipCompanion[References] {
  def apply = new References

  val relationType = RelTypes.references

  type sourceType = Ticket
  type sinkType = Ticket
}

class References extends Relationship {
  val companion = References

  val version = 1
  def updateFrom(oldVersion: Int) = {}

  lazy val referenceType = stringProperty("referencesType")
}