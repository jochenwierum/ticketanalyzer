package de.jowisoftware.mining.model.nodes

import org.neo4j.graphdb.Direction

import de.jowisoftware.mining.model.relationships.{Updates, HasTag, HasComment}
import de.jowisoftware.neo4j.ReadOnlyDatabase
import de.jowisoftware.neo4j.content.{NodeCompanion, IndexAccess}
import helper.MiningNode

object Ticket extends NodeCompanion[Ticket] with IndexAccess[Ticket] {
  def apply = new Ticket

  def find(db: ReadOnlyDatabase[RootNode], uid: String) =
    findInIndex(db, "uid", uid, this)

  def findAll(db: ReadOnlyDatabase[RootNode], uidQuery: String) =
    findMultipleInIndex(db, "uid", uidQuery, this)
}

class Ticket extends MiningNode {
  val version = 1
  def updateFrom(version: Int) = {}

  lazy val uid = stringProperty("uid", "", true)
  lazy val ticketId = intProperty("id")
  lazy val reporter = stringProperty("reporter")
  lazy val text = stringProperty("text")
  lazy val title = stringProperty("title")
  lazy val updateDate = dateProperty("updateDate")
  lazy val creationDate = dateProperty("creationDate")
  lazy val startDate = dateProperty("startDate")
  lazy val dueDate = dateProperty("dueDate")
  lazy val votes = intProperty("votes")
  lazy val eta = floatProperty("eta")
  lazy val environment = stringProperty("environment")
  lazy val build = stringProperty("build")
  lazy val progress = intProperty("progress")
  lazy val spentTime = floatProperty("spentTime")

  def isRecentVersion = neighbors(Direction.INCOMING, Seq(Updates.relationType)).size == 0

  def findComment(id: Int) = TicketComment.find(readableDb, createCommentUid(id))

  def createComment(id: Int) = {
    val node = writableDb.createNode(TicketComment)
    add(node, HasComment)
    node.uid(createCommentUid(id))
    node
  }

  private def createCommentUid(id: Int) = uid().substring(0, uid().lastIndexOf('-') + 1) + id

  def comments = neighbors(Direction.OUTGOING, Seq(HasComment.relationType)) map {
    _.asInstanceOf[TicketComment]
  }

  def tags = neighbors(Direction.OUTGOING, Seq(HasTag.relationType)) map {
    _.asInstanceOf[Tag]
  }
}
