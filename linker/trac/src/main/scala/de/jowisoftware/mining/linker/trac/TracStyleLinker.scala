package de.jowisoftware.mining.linker.trac

import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.linker.{ Linker, LinkEvents }
import de.jowisoftware.mining.model.nodes.{ TicketRepository, Contains, CommitRepository, Commit, Ticket }
import de.jowisoftware.mining.UserOptions

class TracStyleLinker extends Linker {
  def link(tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents) {

    val ticketsCount = tickets.neighbors(Direction.OUTGOING, Seq(Contains.relationType)).size
    val commitsCount = commits.neighbors(Direction.OUTGOING, Seq(Contains.relationType)).size
    val total = ticketsCount + commitsCount

    val scanner = new TextScanner()

    var progress = 0
    for {
      node <- tickets.neighbors(Direction.OUTGOING, Seq(Contains.relationType))
      if (node.isInstanceOf[Ticket])
      ticket = node.asInstanceOf[Ticket]
    } {
      scanner.scan(ticket.text(), events, ticket)
      scanner.scan(ticket.title(), events, ticket)
      progress += 1
      events.reportProgress(progress, total, "Processing tickets")
    }

    for {
      node <- commits.neighbors(Direction.OUTGOING, Seq(Contains.relationType))
      if (node.isInstanceOf[Commit])
      commit = node.asInstanceOf[Commit]
    } {
      scanner.scan(commit.message(), events, commit)
      progress += 1
      events.reportProgress(progress, total, "Processing commits")
    }

    events.finish()
  }

  def userOptions(): UserOptions = new LinkerOptions
}