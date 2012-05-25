package de.jowisoftware.mining.importer

trait ImportEvents {
  def countedTickets(count: Long)
  def countedCommits(count: Long)
  def loadedTicket(tickets: List[TicketData], comments: Seq[TicketCommentData])
  def loadedCommit(commit: CommitData)
  def finish()
}