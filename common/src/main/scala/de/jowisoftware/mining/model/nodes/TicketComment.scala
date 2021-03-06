package de.jowisoftware.mining.model.nodes
import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.RelationshipType
import de.jowisoftware.neo4j.traversing.Traverser
import helper._
import de.jowisoftware.mining.model.relationships.Created

object TicketComment extends IndexedNodeCompanion[TicketComment] {
  def apply = new TicketComment

  override protected val indexedProperties = "name" :: Nil
  protected val primaryProperty = "uid"
}

class TicketComment extends MiningNode {
  val version = 1
  def updateFrom(version: Int) {}

  lazy val uid = stringProperty("uid", "")
  lazy val commentId = intProperty("id")
  lazy val text = stringProperty("text", "")
  lazy val created = dateProperty("created")
  lazy val modified = dateProperty("modified")

  def author = getFirstNeighbor(Direction.INCOMING, Created.relationType, Person)
}