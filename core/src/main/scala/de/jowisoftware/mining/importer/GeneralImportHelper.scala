package de.jowisoftware.mining.importer

import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.model.RootNode

private[importer] object GeneralImportHelper {
  val transactionThreshould = 50
}

private[importer] trait GeneralImportHelper extends ImportEvents {
  private var callCount = 0
  private var currentTransaction = db.startTransaction

  protected val db: Database[RootNode]
  protected def root = currentTransaction.rootNode

  protected def safePointReached {
    callCount += 1
    if (callCount > GeneralImportHelper.transactionThreshould) {
      callCount = 0
      currentTransaction.success
      currentTransaction = db.startTransaction
    }
  }

  protected def transaction(): DBWithTransaction[RootNode] = currentTransaction
  protected def getPerson(name: String) = root.personCollection.findOrCreateChild(name)

  protected def getTicketRepository(name: String) =
    root.ticketRepositoryCollection.findOrCreateChild(name)
  protected def getCommitRepository(name: String) =
    root.commitRepositoryCollection.findOrCreateChild(name)

  def finish() {
    currentTransaction.success
  }
}