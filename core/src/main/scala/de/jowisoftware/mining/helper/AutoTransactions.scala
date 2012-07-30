package de.jowisoftware.mining.helper

import de.jowisoftware.mining.importer.GeneralImportHelper
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.DBWithTransaction

trait AutoTransactions {
  private var callCount = 0
  private var currentTransaction: Option[DBWithTransaction[RootNode]] = None

  protected val transactionThreshould: Int
  protected val db: Database[RootNode]
  protected def root = transaction().rootNode

  protected def safePointReached {
    callCount += 1
    if (callCount > transactionThreshould) {
      callCount = 0
      transaction().success
      currentTransaction = Some(db.startTransaction)
    }
  }

  protected def transaction(): DBWithTransaction[RootNode] = {
    if (!currentTransaction.isDefined)
      currentTransaction = Some(db.startTransaction)
    currentTransaction.get
  }
}