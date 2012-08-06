package de.jowisoftware.mining.analyzer.truck.tickets.filters

import de.jowisoftware.mining.model.nodes.Keyword
import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.Ticket
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.HasStatus
import de.jowisoftware.mining.model.nodes.Status
import de.jowisoftware.mining.linker.StatusType

object StatusFilter extends Filter {
  private val allowed = StatusType.assigned :: StatusType.inReview :: StatusType.qa :: Nil

  def accept(keyword: Keyword, ticket: Ticket, person: Person): Boolean = {
    val status = ticket.getFirstNeighbor(Direction.OUTGOING, HasStatus, Status).map(
      _.logicalType().map(x => StatusType(x))) getOrElse StatusType.ignore
    allowed.contains(status)
  }
}