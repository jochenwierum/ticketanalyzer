package de.jowisoftware.neo4j
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.graphdb.{Transaction => NeoTransaction, Node => NeoNode}
import java.io.File
import de.jowisoftware.util.FileUtils
import org.neo4j.kernel.AbstractGraphDatabase

trait Database[T <: Node] {
  def shutdown
  def inTransaction[S](body: DBWithTransaction[T] => S): S
  
  private[neo4j] def service: AbstractGraphDatabase
}

trait DBWithTransaction[T <: Node] {
  def success
  def failure
  
  val rootNode: T = getRootNode
  protected def getRootNode: T
  
  def createNode[T <: Node](implicit companion: NodeCompanion[T]): T
}

class EmbeddedDatabase[T <: Node](filepath: String, rootCompanion: NodeCompanion[T]) extends Database[T] {
  private[neo4j] val service = new EmbeddedGraphDatabase(filepath)
  
  def inTransaction[S](body: DBWithTransaction[T] => S): S = {
    val tx = service.beginTx()
    val wrapper = new DefaultTransaction(this, tx, rootCompanion)
    
    try {
      return body(wrapper)
    } finally {
      tx.finish()
    }
  }
  
  def shutdown = service.shutdown()
}

class DefaultTransaction[T <: Node](db: Database[T], tx: NeoTransaction,
    rootCompanion: NodeCompanion[T]) extends DBWithTransaction[T] {
  def success = tx.success()
  def failure = tx.failure()
  
  protected def getRootNode: T =
    Node.wrapNeoNode(db.service.getReferenceNode())(rootCompanion)
  
  def createNode[S <: Node](implicit companion: NodeCompanion[S]): S =
    Node.wrapNeoNode(db.service.createNode())
}

object Database {
  def apply[T <: Node](path: String, rootNode: NodeCompanion[T]) = new EmbeddedDatabase(path, rootNode)
  def drop(path: String) = FileUtils.delTree(path)
}