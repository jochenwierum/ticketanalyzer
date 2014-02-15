package de.jowisoftware.neo4j.database

import org.neo4j.graphdb.{ DynamicLabel, NotFoundException, Transaction => NeoTransaction, Node => NeoNode }

import de.jowisoftware.neo4j.{ DBWithTransaction, Database, DatabaseCollection }
import de.jowisoftware.neo4j.content.{ Node, NodeCompanion }
import de.jowisoftware.util.ScalaUtil.withClosable

private[neo4j] class DefaultTransaction(
    db: Database,
    tx: NeoTransaction) extends DBWithTransaction {

  val service = db.service
  var rootNode: Option[NeoNode] = None

  def success = tx.success()
  def failure = tx.failure()

  def createNode[S <: Node](companion: NodeCompanion[S]): S =
    Node.wrapNeoNode(db.service.createNode(), this, companion)

  def getNode[S <: Node](id: Long, companion: NodeCompanion[S]): S =
    Node.wrapNeoNode(db.service.getNodeById(id), this, companion)

  def getUnknownNode(id: Long): Node =
    Node.wrapNeoNode(db.service.getNodeById(id), this).get

  val collections: DatabaseCollection = new DefaultDatabaseCollection(db)

  def rootNode[A <: Node](rootCompanion: NodeCompanion[A]): A = rootNode match {
    case Some(node) => Node.wrapNeoNode(node, this, rootCompanion)
    case None =>
      val optionalNode = withClosable(service.findNodesByLabelAndProperty(
        DynamicLabel.label("function"), "_function", "config").iterator()) execute { nodes =>
        if (nodes.hasNext()) {
          Some(nodes.next())
        } else {
          try {
            Some(service.getNodeById(0))
          } catch {
            case _: NotFoundException => None
          }
        }
      }

      val result = optionalNode match {
        case Some(node) =>
          Node.wrapNeoNode(node, this, rootCompanion)
        case None =>
          createNode(rootCompanion)
      }
      rootNode = Some(result.content)
      result
  }
}
