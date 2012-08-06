package de.jowisoftware.mining.analyzer.truck.tickets.filters

import de.jowisoftware.mining.model.nodes.Keyword
import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.Ticket
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.HasStatus
import de.jowisoftware.mining.model.nodes.Status

class StatusFilter(allowed: String*) extends Filter {
  def accept(keyword: Keyword, ticket: Ticket, person: Person): Boolean = {
    val status = ticket.getFirstNeighbor(Direction.OUTGOING, HasStatus, Status) map (_.name()) getOrElse ""
    allowed.contains(status)
  }
}