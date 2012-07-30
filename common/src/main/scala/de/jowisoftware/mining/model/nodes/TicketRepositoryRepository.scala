package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.neo4j.content.NodeCompanion
import helper._

object TicketRepositoryRepository extends NodeCompanion[TicketRepositoryRepository] {
  def apply = new TicketRepositoryRepository
}

class TicketRepositoryRepository extends MiningNode with EmptyNode with HasChildWithName[TicketRepository] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, TicketRepository)

  def children = children(Contains, TicketRepository)
}