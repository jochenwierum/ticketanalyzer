package de.jowisoftware.mining.analyzer.truck.tickets.filters

import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.Ticket
import de.jowisoftware.mining.model.nodes.Keyword

class FilterAndCollection(initialFilters: Filter*) extends Filter {
  private var filters: List[Filter] = initialFilters.toList

  def add(filter: Filter) = {
    filters = filter :: filters
    this
  }

  def accept(keyword: Keyword, ticket: Ticket, person: Person): Boolean =
    filters.forall(_.accept(keyword, ticket, person))
}