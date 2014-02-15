package de.jowisoftware.mining.importer

import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.helper.AutoTransactions
import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.model.nodes.TicketRepository
import de.jowisoftware.mining.model.nodes.CommitRepository

private[importer] trait GeneralImportHelper extends ImportEvents with AutoTransactions {
  val transactionThreshould = 50
  protected def getPerson(name: String) = find(Person, name)

  protected def getTicketRepository(name: String) =
    find(TicketRepository, name)
  protected def getCommitRepository(name: String) =
    find(CommitRepository, name)

  def finish() {
    transaction.success
  }
}