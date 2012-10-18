package de.jowisoftware.mining.analyzer.truck.tickets.filters

import de.jowisoftware.mining.model.nodes.Keyword
import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.Ticket
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.HasStatus
import de.jowisoftware.mining.model.nodes.Status
import de.jowisoftware.mining.model.relationships.Updates

class WroteCommentFilter(count: Int) extends Filter {
  require(count > 0)

  def accept(keyword: Keyword, ticket: Ticket, person: Person): Boolean =
    ticket.allVersions.count(_.comments.find(person == _.author.getOrElse(null)).isDefined) >= count
}