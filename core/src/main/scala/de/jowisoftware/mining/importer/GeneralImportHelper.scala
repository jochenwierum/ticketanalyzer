package de.jowisoftware.mining.importer

import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.helper.AutoTransactions

private[importer] trait GeneralImportHelper extends ImportEvents with AutoTransactions {
  val transactionThreshould = 50
  protected def getPerson(name: String) = root.personCollection.findOrCreateChild(name)

  protected def getTicketRepository(name: String) =
    root.ticketRepositoryCollection.findOrCreateChild(name)
  protected def getCommitRepository(name: String) =
    root.commitRepositoryCollection.findOrCreateChild(name)

  def finish() {
    transaction.success
  }
}