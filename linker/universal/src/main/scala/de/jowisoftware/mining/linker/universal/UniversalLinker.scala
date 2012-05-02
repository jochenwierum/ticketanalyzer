package de.jowisoftware.mining.linker.universal

import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.linker.{ Linker, LinkEvents }
import de.jowisoftware.mining.model.{ TicketRepository, Ticket, Contains, CommitRepository }
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.model.Commit
import de.jowisoftware.mining.model.Ticket
import de.jowisoftware.mining.linker.LinkType

class UniversalLinker extends Linker {
  def link(tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents) {

    val total = tickets.neighbors(Direction.OUTGOING, Seq(Contains.relationType)).size
    val scanner = new DependencyScanner()

    var progress = 0
    for {
      node <- tickets.neighbors(Direction.OUTGOING, Seq(Contains.relationType))
      if (node.isInstanceOf[Ticket])
      ticket = node.asInstanceOf[Ticket]
    } {
      scanner.scan(ticket.dependsOn(), events, ticket, LinkType.Blocks)
      scanner.scan(ticket.blocks(), events, ticket, LinkType.DependsOn)
      progress += 1
      events.reportProgress(progress, total, "Processing tickets")
    }
    events.finish()
  }

  def userOptions(): UserOptions = new LinkerOptions
}