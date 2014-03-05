package de.jowisoftware.neo4j.database

import org.neo4j.graphdb.{ DynamicLabel, Label, Node => NeoNode, NotFoundException, Transaction => NeoTransaction }
import de.jowisoftware.neo4j.{ CypherService, DBWithTransaction, Database }
import de.jowisoftware.neo4j.content.{ Node, NodeCompanion }
import de.jowisoftware.util.ScalaUtil.withClosable
import org.neo4j.graphdb.ResourceIterable
import org.neo4j.shell.kernel.apps.Dbinfo

private[neo4j] class DefaultTransaction(
    db: Database,
    tx: NeoTransaction,
    protected val cypherService: CypherService) extends DBWithTransaction {

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

  def rootNode[A <: Node](rootCompanion: NodeCompanion[A]): A = rootNode match {
    case Some(node) => Node.wrapNeoNode(node, this, rootCompanion)
    case None =>
      val optionalNode = withClosable(service.findNodesByLabelAndProperty(
        DynamicLabel.label("function"), "_function", "config").iterator()) { nodes =>
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

  def findNodesByLabelAndProperty(label: Label, key: String, value: AnyRef): ResourceIterable[NeoNode] =
    service.findNodesByLabelAndProperty(label, key, value)

  def inTransaction[A](body: DBWithTransaction => A) = db.inTransaction(body)
}
