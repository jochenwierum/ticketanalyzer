package de.jowisoftware.mining.model
import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.RelationshipType
import de.jowisoftware.neo4j.traversing.Traverser
import helper._

object TicketComment extends NodeCompanion[TicketComment] {
  def apply = new TicketComment
}

class TicketComment extends MiningNode {
  val version = 1
  def updateFrom(version: Int) {}

  lazy val commentId = intProperty("id")
  lazy val text = stringProperty("text", "", true)
  lazy val created = dateProperty("created")
  lazy val modified = dateProperty("modified")
}