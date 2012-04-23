package de.jowisoftware.neo4j.database

import org.neo4j.kernel.EmbeddedGraphDatabase
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.content.Node

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
