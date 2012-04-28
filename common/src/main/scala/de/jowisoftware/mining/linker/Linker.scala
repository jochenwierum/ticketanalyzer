package de.jowisoftware.mining.linker

import de.jowisoftware.mining.model.{ TicketRepository, CommitRepository }
import de.jowisoftware.mining.UserOptions

trait Linker {
  def link(tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents)
  def userOptions: UserOptions
}