package de.jowisoftware.mining.importer

import de.jowisoftware.mining.helper.AutoTransactions
import de.jowisoftware.mining.model.nodes.{CommitRepository, Person, TicketRepository}

private[importer] trait GeneralImportHelper extends ImportEvents with AutoTransactions {
  val transactionThreshold = 50
  protected def getPerson(name: String) = find(Person, name)

  protected def getTicketRepository(name: String) =
    find(TicketRepository, name)
  protected def getCommitRepository(name: String) =
    find(CommitRepository, name)

  def finish(): Unit = {
    transaction.success()
  }
}
