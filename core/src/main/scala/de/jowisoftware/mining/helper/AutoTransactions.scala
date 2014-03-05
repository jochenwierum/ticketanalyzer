package de.jowisoftware.mining.helper

import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }
import de.jowisoftware.neo4j.content.{ IndexedNodeCompanion, NodeCompanion }
import de.jowisoftware.neo4j.content.Node

trait AutoTransactions {
  private var callCount = 0
  private var currentTransaction: Option[DBWithTransaction] = None

  protected val transactionThreshould: Int
  protected val db: Database

  protected def safePointReached {
    callCount += 1
    if (callCount > transactionThreshould) {
      callCount = 0
      transaction().success
      currentTransaction = Some(db.startTransaction)
    }
  }

  protected def transaction(): DBWithTransaction = {
    if (!currentTransaction.isDefined)
      currentTransaction = Some(db.startTransaction)
    currentTransaction.get
  }

  def find[A <: Node](repositoryCompanion: IndexedNodeCompanion[A], name: String): A =
    repositoryCompanion.findOrCreate(transaction, name)
}