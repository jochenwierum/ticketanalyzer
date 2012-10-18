package de.jowisoftware.mining.analyzer.truck.tickets.filters

import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.Ticket
import de.jowisoftware.mining.model.nodes.Keyword

class PersonChangedStatusFilter(count: Int) extends Filter {
  require(count > 0)

  def accept(keyword: Keyword, ticket: Ticket, person: Person): Boolean =
    ticket.allVersions.count(_.change map { change =>
      (change.source == person) && (change.changes() contains "status")
    } getOrElse false) >= count
}