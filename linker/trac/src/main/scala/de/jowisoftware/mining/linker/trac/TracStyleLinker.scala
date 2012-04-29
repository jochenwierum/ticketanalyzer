package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.model.TicketRepository
import de.jowisoftware.mining.linker.Linker
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.linker.LinkEvents
import de.jowisoftware.mining.model.CommitRepository

class TracStyleLinker extends Linker {
  def link(tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents) {

    events.progress(1, 100, "Doing something")
    Thread.sleep(2000)
    events.progress(50, 100, "Doing something else...")
    Thread.sleep(2000)
    events.finish()
  }

  def userOptions(): UserOptions = new LinkerOptions
}