package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.linker.{LinkEvents, Linker}
import de.jowisoftware.mining.model.nodes.{CommitRepository, TicketRepository}
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.neo4j.DBWithTransaction
import org.neo4j.graphdb.Direction

class TracStyleLinker extends Linker {
  def link(transaction: DBWithTransaction, tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents): Unit = {

    val ticketsCount = tickets.neighbors(Direction.OUTGOING, Seq(Contains.relationType)).size
    val commitsCount = commits.neighbors(Direction.OUTGOING, Seq(Contains.relationType)).size
    val total = ticketsCount + commitsCount

    val scanner = new TextScanner()

    var progress = 0
    for {
      ticket <- tickets.tickets
    } {
      scanner.scan(transaction, ticket.text(), events, ticket, commits)
      scanner.scan(transaction, ticket.title(), events, ticket, commits)
      ticket.comments.foreach { c => scanner.scan(transaction, c.text(), events, ticket, commits) }
      progress += 1
      events.reportProgress(progress, total, "Processing tickets")
    }

    for {
      commit <- commits.commits
    } {
      scanner.scan(transaction, commit.message(), events, commit, commits)
      progress += 1
      events.reportProgress(progress, total, "Processing commits")
    }

    events.finish()
  }

  def userOptions: UserOptions = new LinkerOptions
}
