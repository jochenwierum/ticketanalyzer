package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.relationships.Contains
import org.neo4j.graphdb.Direction
import de.jowisoftware.neo4j.content.NodeCompanion
import helper._
import com.sun.corba.se.spi.ior.Writeable
import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import de.jowisoftware.neo4j.content.IndexedNodeInfo

object TicketRepository extends IndexedNodeCompanion[TicketRepository] {
  def apply = new TicketRepository
  val indexInfo = IndexedNodeInfo(IndexedNodeInfo.Labels.ticketRepository)
}

class TicketRepository extends MiningNode with HasName with EmptyNode {
  def obtainTicket(id: Int, version: Int): Ticket = {
    val uid = name()+"-"+id+"-"+version
    Ticket.find(readableDb, uid) match {
      case Some(ticket) => ticket
      case None =>
        val ticket = writableDb.createNode(Ticket)
        ticket.ticketId(id)
        ticket.uid(uid)
        this.add(ticket, Contains)
        ticket
    }
  }

  def findRecentVersionOf(tId: Long): Option[Ticket] =
    findAllVersionsOf(tId).reduceOption {
      (t1, t2) =>
        val v1 = t1.uid().substring(t1.uid().lastIndexOf('-') + 1).toInt
        val v2 = t2.uid().substring(t1.uid().lastIndexOf('-') + 1).toInt
        if (v1 > v2) t1 else t2
    }

  def findAllVersionsOf(tId: Long) =
    Ticket.findAll(readableDb, name()+"-"+tId+"-*")

  def tickets =
    for {
      potentialTicket <- neighbors(Direction.OUTGOING, Seq(Contains.relationType))
      if (potentialTicket.isInstanceOf[Ticket])
      ticket = potentialTicket.asInstanceOf[Ticket]
    } yield ticket
}