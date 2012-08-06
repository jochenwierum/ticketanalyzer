package de.jowisoftware.neo4j.database

import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.content.NodeCompanion
import org.neo4j.graphdb.{ Transaction => NeoTransaction }

private[neo4j] class DefaultTransaction[T <: Node](
    db: Database[T],
    tx: NeoTransaction,
    rootCompanion: NodeCompanion[T]) extends DBWithTransaction[T] {
  val service = db.service

  def success = tx.success()
  def failure = tx.failure()

  lazy val rootNode: T =
    Node.wrapNeoNode(db.service.getReferenceNode(), this, rootCompanion)

  def createNode[S <: Node](companion: NodeCompanion[S]): S =
    Node.wrapNeoNode(db.service.createNode(), this, companion)

  def getNode[S <: Node](id: Long, companion: NodeCompanion[S]): S =
    Node.wrapNeoNode(db.service.getNodeById(id), this, companion)

  def getUnknownNode(id: Long): Node =
    Node.wrapNeoNode(db.service.getNodeById(id), this).get

}
