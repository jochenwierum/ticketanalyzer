package de.jowisoftware.mining.analyzer.truck.tickets.filters

import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.Ticket
import de.jowisoftware.mining.model.nodes.Keyword
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.Links
import de.jowisoftware.mining.model.nodes.Commit
import de.jowisoftware.mining.model.relationships.Owns

object PersonHasComittedFilter extends Filter {
  def accept(keyword: Keyword, ticket: Ticket, person: Person): Boolean =
    ticket.neighbors(Direction.BOTH, Seq(Links.relationType)).exists {
      case commit: Commit =>
        commit.getFirstNeighbor(Direction.OUTGOING, Owns, Person).get == person // ouch!
      case _ => false
    }
}