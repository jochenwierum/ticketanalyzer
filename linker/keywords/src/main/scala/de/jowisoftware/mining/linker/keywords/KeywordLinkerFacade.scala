package de.jowisoftware.mining.linker.keywords

import de.jowisoftware.mining.linker.Linker
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.linker.LinkEvents
import de.jowisoftware.mining.model.nodes.TicketRepository
import de.jowisoftware.mining.model.nodes.CommitRepository
import de.jowisoftware.neo4j.Database

class KeywordLinkerFacade extends Linker {
  def userOptions(): UserOptions = new KeywordLinkerOptions
  def link(db: Database, tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents) =
    new KeywordLinker(tickets, commits, options, events).link()
}