package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object ChangedTicket extends RelationshipCompanion[ChangedTicket] {
  def apply = new ChangedTicket

  val relationType = RelTypes.changedTicket

  type sourceType = Person
  type sinkType = Ticket
}

class ChangedTicket extends Relationship {
  val companion = ChangedTicket

  val version = 1
  def updateFrom(version: Int) = {}

  lazy val changes = stringArrayProperty("changes")
}
