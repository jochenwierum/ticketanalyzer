package de.jowisoftware.mining.analyzer.truck.tickets.filters

import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.Ticket
import de.jowisoftware.mining.model.nodes.Keyword

object PersonChangedStatus extends Filter {
  def accept(keyword: Keyword, ticket: Ticket, person: Person): Boolean =
    ticket.allVersions.find(_.change map (_.changes() contains "status") getOrElse false).isDefined
}