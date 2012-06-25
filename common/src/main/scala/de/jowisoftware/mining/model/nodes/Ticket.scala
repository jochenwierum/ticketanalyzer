package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.relationships.{ Contains, Updates }
import org.neo4j.graphdb.Direction
import de.jowisoftware.neo4j.content.NodeCompanion
import helper._
import de.jowisoftware.neo4j.DBWithTransaction

object Ticket extends NodeCompanion[Ticket] {
  def apply = new Ticket

  def find(db: DBWithTransaction[RootNode], uid: String) =
    findInIndex(db, "uid", uid)

  def findAll(db: DBWithTransaction[RootNode], uidQuery: String) =
    findMultipleInIndex(db, "uid", uidQuery)
}

class Ticket extends MiningNode {
  val version = 1
  def updateFrom(version: Int) = {}

  lazy val uid = stringProperty("uid", "", true)
  lazy val ticketId = intProperty("id")
  lazy val reporter = stringProperty("reporter")
  lazy val text = stringProperty("text")
  lazy val title = stringProperty("title")
  lazy val updateDate = dateProperty("time")
  lazy val creationDate = dateProperty("time")
  lazy val votes = intProperty("votes")
  lazy val eta = intProperty("eta")
  lazy val environment = stringProperty("environment")
  lazy val build = stringProperty("build")

  def isRecentVersion = neighbors(Direction.INCOMING, Seq(Updates.relationType)).size == 0

  def findComment(id: Int) = TicketComment.find(db, createCommentUid(id))

  def createComment(id: Int) = {
    val node = db.createNode(TicketComment)
    node.uid(createCommentUid(id))
    node
  }

  private def createCommentUid(id: Int) = uid().substring(0, uid().lastIndexOf('-') + 1) + id
}