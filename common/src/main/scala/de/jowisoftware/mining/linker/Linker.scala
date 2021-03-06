package de.jowisoftware.mining.linker

import de.jowisoftware.mining.model.nodes.{ TicketRepository, CommitRepository }
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.DBWithTransaction

trait Linker {
  def link(transaction: DBWithTransaction, tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents)
  def userOptions: UserOptions
}