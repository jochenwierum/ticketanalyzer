package de.jowisoftware.mining.analyzer.truck.tickets.filters

import de.jowisoftware.mining.model.nodes.Keyword
import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.Ticket
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.RootOf
import de.jowisoftware.mining.model.relationships.Reported

object NonReporterFilter extends Filter {
  def accept(keyword: Keyword, ticket: Ticket, person: Person): Boolean = {
    val isOtherPerson = for {
      rootTicket <- ticket.getFirstNeighbor(Direction.OUTGOING, RootOf.relationType, Ticket)
      reporter <- rootTicket.getFirstNeighbor(Direction.INCOMING, Reported.relationType, Person)
    } yield {
      person != reporter
    }
    isOtherPerson getOrElse true
  }
}