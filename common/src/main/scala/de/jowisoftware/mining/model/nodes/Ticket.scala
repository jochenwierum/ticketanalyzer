package de.jowisoftware.mining.model.nodes

import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.{ Updates, HasTag, HasComment }
import de.jowisoftware.neo4j.ReadOnlyDatabase
import de.jowisoftware.neo4j.content.{ NodeCompanion, IndexAccess }
import helper.MiningNode
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.mining.model.relationships.RootOf
import de.jowisoftware.mining.model.relationships.ChangedTicket
import de.jowisoftware.mining.model.relationships.HasStatus

object Ticket extends NodeCompanion[Ticket] with IndexAccess[Ticket] {
  def apply = new Ticket

  def find(db: ReadOnlyDatabase[RootNode], uid: String) =
    findInIndex(db, "uid", uid, this)

  def findAll(db: ReadOnlyDatabase[RootNode], uidQuery: String) =
    findMultipleInIndex(db, "uid", uidQuery, this)
}

class Ticket extends MiningNode {
  val version = 3
  def updateFrom(oldVersion: Int) = {
    if (oldVersion < 3) {
      if (oldVersion == 2) {
        for (rootRel <- getFirstRelationship(Direction.BOTH, RootOf)) {
          rootRel.delete()
        }
      }

      def findRoot(e: Node): Node =
        e.neighbors(Direction.OUTGOING, Seq(Updates.relationType)).toList match {
          case Nil => e
          case x :: tail => findRoot(x)
        }

      val parent = findRoot(this)
      if (parent != this)
        parent.add(this, RootOf)
    }
  }

  lazy val uid = stringProperty("uid", "", true)
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
