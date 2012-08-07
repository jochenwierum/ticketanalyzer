package de.jowisoftware.mining.analyzer.truck.tickets.filters

protected class FilterCollection(initialFilters: Filter*) {
  private var filters: List[Filter] = initialFilters.toList

  def add(filter: Filter) = {
    filters = filter :: filters
    this
  }

  def allFilters = filters
}