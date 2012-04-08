package de.jowisoftware.mining.importer

trait ImportEvents {
  def countedTickets(count: Long)
  def countedCommits(count: Long)
  def loadedTicket(ticket: TicketData)
  def loadedCommit(commit: CommitData)
  def finish()
}