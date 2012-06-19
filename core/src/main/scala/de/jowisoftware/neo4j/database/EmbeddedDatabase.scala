package de.jowisoftware.neo4j.database

import scala.collection.JavaConversions.iterableAsScalaIterable
import org.neo4j.kernel.EmbeddedGraphDatabase
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.DBWithTransaction
import org.neo4j.server.WrappingNeoServerBootstrapper
import scala.collection.immutable.SortedSet
import scala.annotation.tailrec
import org.neo4j.kernel.AbstractGraphDatabase
import de.jowisoftware.util.FileUtils
import de.jowisoftware.neo4j.Database
import java.io.File

class EmbeddedDatabase[T <: Node](filepath: File, rootCompanion: NodeCompanion[T]) extends Database[T] {
  var service: AbstractGraphDatabase = _
  init()

  protected def init() {
    service = new EmbeddedGraphDatabase(filepath.getAbsolutePath)
  }

  def inTransaction[S](body: DBWithTransaction[T] => S): S = {
    val tx = service.beginTx()
    val wrapper = new DefaultTransaction(this, tx, rootCompanion)

    try {
      return body(wrapper)
    } finally {
      tx.finish()
    }
  }

  def startTransaction: DBWithTransaction[T] = {
    val tx = service.beginTx()
    new AutonomousTransaction(this, tx, rootCompanion)
  }

  def deleteContent = {
    shutdown()
    FileUtils.delTree(filepath)
    init()
  }

  def shutdown() {
    service.shutdown()
  }
}
