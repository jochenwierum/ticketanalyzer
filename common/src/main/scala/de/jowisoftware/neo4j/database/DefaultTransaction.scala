package de.jowisoftware.neo4j.database

import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.content.NodeCompanion
import org.neo4j.graphdb.{Transaction => NeoTransaction}

private[neo4j] class DefaultTransaction[T <: Node](
    db: Database[T],
    tx: NeoTransaction,
    rootCompanion: NodeCompanion[T]
  ) extends DBWithTransaction[T] {
  private[neo4j] val service = db.service

  def success = tx.success()
  def failure = tx.failure()

  protected def getRootNode: T =
    Node.wrapNeoNode(db.service.getReferenceNode(), this)(rootCompanion)

  def createNode[S <: Node](implicit companion: NodeCompanion[S]): S =
    Node.wrapNeoNode(db.service.createNode(), this)

  def getNode[S <: Node](id: Long)(implicit companion: NodeCompanion[S]) : S =
    Node.wrapNeoNode(db.service.getNodeById(id), this)
}
