package de.jowisoftware.mining.model.nodes

import org.neo4j.graphdb.Direction

import de.jowisoftware.neo4j.content.NodeCompanion
import helper._

object TicketRepository extends NodeCompanion[TicketRepository] {
  def apply = new TicketRepository
}

class TicketRepository extends MiningNode with HasName with EmptyNode {
  def obtainTicket(id: Int, version: Int): Ticket = {
    val ticket = db.createNode(Ticket)
    ticket.ticketId(id)
    ticket.uid(name()+"-"+id+"-"+version)
    this.add(ticket)(Contains)
    ticket
  }

  def findRecentVersionOf(tId: Int): Option[Ticket] = {
    neighbors(Direction.OUTGOING).find { node =>
      val ticket = node.asInstanceOf[Ticket]
      ticket.ticketId() == tId && ticket.isRecentVersion
    }.asInstanceOf[Option[Ticket]]
  }
}