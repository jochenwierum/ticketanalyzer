package de.jowisoftware.mining.importer

trait ImportEvents {
  def countedTickets(count: Long)
  def countedCommits(count: Long)

  /**
    * Loads a ticket with all its versions
    * @param tickets list of versions, ordered by date from old to new
    */
  def loadedTicket(repository: String, versions: List[TicketData], comments: Seq[TicketCommentData])
  def loadedCommit(repository: String, commit: CommitData)
  def finish()
}