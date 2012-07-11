package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.relationships.Contains
import org.neo4j.graphdb.Direction

import de.jowisoftware.neo4j.content.NodeCompanion
import helper._

object TicketRepository extends NodeCompanion[TicketRepository] {
  def apply = new TicketRepository
}

class TicketRepository extends MiningNode with HasName with EmptyNode {
  def obtainTicket(id: Int, version: Int): Ticket = {
    val uid = name()+"-"+id+"-"+version
    Ticket.find(db, uid) match {
      case Some(ticket) => ticket
      case None =>
        val ticket = db.createNode(Ticket)
        ticket.ticketId(id)
        ticket.uid(uid)
        this.add(ticket, Contains)
        ticket
    }
  }

  def findRecentVersionOf(tId: Int): Option[Ticket] =
    Ticket.findAll(db, name()+"-"+tId+"-*").reduceOption {
      (t1, t2) =>
        val v1 = t1.uid().substring(t1.uid().lastIndexOf('-') + 1).toInt
        val v2 = t2.uid().substring(t1.uid().lastIndexOf('-') + 1).toInt
        if (v1 > v2) t1 else t2
    }
}