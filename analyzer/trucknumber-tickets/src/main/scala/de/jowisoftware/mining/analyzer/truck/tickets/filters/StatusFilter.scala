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
    val status = ticket.status
      .flatMap(_.logicalType().map(StatusType.apply)) getOrElse StatusType.ignore
    allowed.contains(status)
  }
}