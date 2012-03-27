package de.jowisoftware.neo4j
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.graphdb.{Transaction => NeoTransaction, Node => NeoNode}
import java.io.File
import de.jowisoftware.util.FileUtils
import org.neo4j.kernel.AbstractGraphDatabase

trait Database {
  def shutdown
  def inTransaction[T](body: DBWithTransaction => T): T
  
  private[neo4j] def service: AbstractGraphDatabase
}

trait DBWithTransaction {
  def success
  def failure
  
  def rootNode[T <: Node](implicit companion: NodeCompanion[T]): T
  def createNode[T <: Node](implicit companion: NodeCompanion[T]): T
}

class EmbeddedDatabase(filepath: String) extends Database {
  private[neo4j] val service = new EmbeddedGraphDatabase(filepath)
  
  def inTransaction[T](body: DBWithTransaction => T): T = {
    val tx = service.beginTx()
    val wrapper = new DefaultTransaction(this, tx)
    
    try {
      return body(wrapper)
    } finally {
      tx.finish()
    }
  }
  
  def shutdown = service.shutdown()
}

class DefaultTransaction(db: Database, tx: NeoTransaction) extends DBWithTransaction {
  def success = tx.success()
  def failure = tx.failure()
  
  def rootNode[T <: Node](implicit companion: NodeCompanion[T]): T =
    Node.wrapNeoNode(db.service.getReferenceNode())
  
  def createNode[T <: Node](implicit companion: NodeCompanion[T]): T =
    Node.wrapNeoNode(db.service.createNode())
}

object Database {
  def apply(path: String) = new EmbeddedDatabase(path)
  def drop(path: String) = FileUtils.delTree(path)
}