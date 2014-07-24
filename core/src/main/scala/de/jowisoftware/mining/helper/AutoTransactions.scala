package de.jowisoftware.mining.helper

import de.jowisoftware.neo4j.content.{IndexedNodeCompanion, Node}
import de.jowisoftware.neo4j.{DBWithTransaction, Database}

trait AutoTransactions {
  private var callCount = 0
  private var currentTransaction: Option[DBWithTransaction] = None

  protected val transactionThreshold: Int
  protected val db: Database

  protected def safePointReached():Unit = {
    callCount += 1
    if (callCount > transactionThreshold) {
      callCount = 0
      transaction.success()
      currentTransaction = Some(db.startTransaction)
    }
  }

  protected def transaction: DBWithTransaction = {
    if (!currentTransaction.isDefined)
      currentTransaction = Some(db.startTransaction)
    currentTransaction.get
  }

  def find[A <: Node](repositoryCompanion: IndexedNodeCompanion[A], name: String): A =
    repositoryCompanion.findOrCreate(transaction, name)
}
