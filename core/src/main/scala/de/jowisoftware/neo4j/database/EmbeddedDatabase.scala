package de.jowisoftware.neo4j.database

import java.io.File

import scala.collection.JavaConversions.mutableMapAsJavaMap
import scala.collection.mutable

import org.neo4j.kernel.{ AbstractGraphDatabase, EmbeddedGraphDatabase }

import de.jowisoftware.neo4j.{ DBWithTransaction, Database }
import de.jowisoftware.neo4j.content.{ Node, NodeCompanion }
import de.jowisoftware.util.FileUtils

class EmbeddedDatabase[T <: Node](filepath: File, rootCompanion: NodeCompanion[T]) extends Database[T] {
  private var databaseService: AbstractGraphDatabase = _
  init()
  addShutdownHook()

  def service = databaseService

  protected def init() {
    val config = mutable.Map("keep_logical_logs" -> "1 hours")
    databaseService = new EmbeddedGraphDatabase(filepath.getAbsolutePath,
      mutableMapAsJavaMap(config))

    // make sure the root note is initialized
    inTransaction { transaction =>
      transaction.rootNode
      transaction.success
    }
  }

  def inTransaction[S](body: DBWithTransaction[T] => S): S = {
    val tx = service.beginTx()
    val wrapper = new DefaultTransaction(this, tx, rootCompanion)

    try {
      body(wrapper)
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
    try {
      service.shutdown()
    } catch {
      case e: Exception =>
      // we can't do anything here, so we ignore the exception
    }
  }

  private def addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      override def run() =
        shutdown()
    })
  }

  def rootNode: T =
    Node.wrapNeoNode(service.getReferenceNode, this, rootCompanion)

  def getUnknownNode(id: Long): Node =
    Node.wrapNeoNode(service.getNodeById(id), this).get

  def getNode[S <: Node](id: Long, companion: NodeCompanion[S]): S =
    Node.wrapNeoNode(service.getNodeById(id), this, companion)
}
