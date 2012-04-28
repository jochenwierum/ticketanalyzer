package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.model.TicketRepository
import de.jowisoftware.mining.linker.Linker
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.linker.LinkEvents
import de.jowisoftware.mining.model.CommitRepository

class TracStyleLinker extends Linker {
  def link(tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents) {}
  def userOptions(): UserOptions = new LinkerOptions
}