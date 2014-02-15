package de.jowisoftware.mining.linker.trac

import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.linker.{ Linker, LinkEvents }
import de.jowisoftware.mining.model.nodes.{ TicketRepository, CommitRepository, Commit, Ticket }
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.neo4j.Database

class TracStyleLinker extends Linker {
  def link(db: Database, tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents) {

    val ticketsCount = tickets.neighbors(Direction.OUTGOING, Seq(Contains.relationType)).size
    val commitsCount = commits.neighbors(Direction.OUTGOING, Seq(Contains.relationType)).size
    val total = ticketsCount + commitsCount

    val scanner = new TextScanner()

    var progress = 0
    for {
      ticket <- tickets.tickets
    } {
      scanner.scan(ticket.text(), events, ticket, commits)
      scanner.scan(ticket.title(), events, ticket, commits)
      ticket.comments.foreach { c => scanner.scan(c.text(), events, ticket, commits) }
      progress += 1
      events.reportProgress(progress, total, "Processing tickets")
    }

    for {
      commit <- commits.commits
    } {
      scanner.scan(commit.message(), events, commit, commits)
      progress += 1
      events.reportProgress(progress, total, "Processing commits")
    }

    events.finish()
  }

  def userOptions(): UserOptions = new LinkerOptions
}