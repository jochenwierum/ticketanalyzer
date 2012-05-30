package de.jowisoftware.neo4j.database

import scala.collection.JavaConversions.iterableAsScalaIterable
import org.neo4j.kernel.EmbeddedGraphDatabase
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.DBWithTransaction
import org.neo4j.server.WrappingNeoServerBootstrapper

class EmbeddedDatabase[T <: Node](filepath: String, rootCompanion: NodeCompanion[T]) extends Database[T] {
  val service = new EmbeddedGraphDatabase(filepath)

  private val server = new WrappingNeoServerBootstrapper(service)
  server.start()

  def inTransaction[S](body: DBWithTransaction[T] => S): S = {
    val tx = service.beginTx()
    val wrapper = new DefaultTransaction(this, tx, rootCompanion)

    try {
      return body(wrapper)
    } finally {
      tx.finish()
    }
  }

  def deleteContent = inTransaction { trans =>
    service.getAllNodes.foreach { n =>
      n.getRelationships.foreach { r => r.delete }
      if (n.getId != 0)
        n.delete
      else
        n.getPropertyKeys.foreach(k => n.removeProperty(k))
    }
    trans.success
  }

  def shutdown() {
    server.stop()
    service.shutdown()
  }
}
