package de.jowisoftware.mining.analyzer.truck.tickets.filters

import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.Ticket
import de.jowisoftware.mining.model.nodes.Keyword

class AndCollectionFilter(initialFilters: Filter*)
    extends FilterCollection(initialFilters: _*) with Filter {

  def accept(keyword: Keyword, ticket: Ticket, person: Person): Boolean =
    allFilters.forall(_.accept(keyword, ticket, person))
}