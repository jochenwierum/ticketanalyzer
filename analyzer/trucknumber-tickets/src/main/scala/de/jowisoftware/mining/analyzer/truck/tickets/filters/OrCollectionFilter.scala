package de.jowisoftware.mining.analyzer.truck.tickets.filters

import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.Ticket
import de.jowisoftware.mining.model.nodes.Keyword

class OrCollectionFilter(initialFilters: Filter*)
    extends FilterCollection(initialFilters: _*) with Filter {

  def accept(keyword: Keyword, ticket: Ticket, person: Person): Boolean =
    allFilters.exists(_.accept(keyword, ticket, person))
}