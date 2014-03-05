package de.jowisoftware.mining.model.nodes

import org.neo4j.graphdb.Direction

import de.jowisoftware.mining.model.relationships.{ ChangedTicket, HasComment, HasStatus, HasTag, Updates }
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.{ IndexedNodeCompanion, RegexIndexAccess }
import de.jowisoftware.neo4j.content.Relationship.relationship2RelationshipType
import helper.MiningNode

object Ticket extends IndexedNodeCompanion[Ticket] with RegexIndexAccess[Ticket] {
  def apply = new Ticket

  protected val primaryProperty = "uid"

  def findAll(db: DBWithTransaction, uidQuery: String) =
    findMultipleByPatternInIndex(db, "uid", uidQuery)
}

class Ticket extends MiningNode with TicketUpdates {
  val version = 4
  def updateFrom(oldVersion: Int) = {
    if (oldVersion < 2) updateToV2()
    if (oldVersion < 3) updateToV3()
    if (oldVersion < 4) updateToV4()
  }

  lazy val uid = stringProperty("uid", "")
  lazy val ticketId = intProperty("id")
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
  def isRootVersion = neighbors(Direction.OUTGOING, Seq(Updates.relationType)).size == 0

  def findComment(id: Int) = readableDb.inTransaction { t => TicketComment.find(t, createCommentUid(id)) }

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

  def allVersions = {
    var newTicket: Option[Ticket] = Some(this)
    var allTickets: List[Ticket] = Nil

    while (newTicket.isDefined) {
      allTickets = newTicket.get :: allTickets
      newTicket = newTicket.get.getFirstNeighbor(Direction.OUTGOING, Updates.relationType, Ticket)
    }

    newTicket = getFirstNeighbor(Direction.INCOMING, Updates.relationType, Ticket)
    while (newTicket.isDefined) {
      allTickets = newTicket.get :: allTickets
      newTicket = newTicket.get.getFirstNeighbor(Direction.INCOMING, Updates.relationType, Ticket)
    }

    allTickets
  }

  def change = getFirstRelationship(Direction.INCOMING, ChangedTicket)
  def status = getFirstNeighbor(Direction.OUTGOING, HasStatus, Status)
}
