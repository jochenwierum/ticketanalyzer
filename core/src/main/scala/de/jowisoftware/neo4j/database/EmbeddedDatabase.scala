package de.jowisoftware.neo4j.database

import java.io.File
import scala.collection.JavaConversions.mutableMapAsJavaMap
import scala.collection.mutable
import org.neo4j.kernel.{ AbstractGraphDatabase, EmbeddedGraphDatabase }
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }
import de.jowisoftware.neo4j.content.{ Node, NodeCompanion }
import de.jowisoftware.util.FileUtils
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.helpers.Settings.DefaultSetting
import org.neo4j.helpers.Settings
import org.neo4j.kernel.configuration.Config
import org.neo4j.graphdb.factory.GraphDatabaseSettings

class EmbeddedDatabase[T <: Node](filepath: File, rootCompanion: NodeCompanion[T]) extends Database[T] {
  private var databaseService: GraphDatabaseService = _
  init()
  addShutdownHook()

  def service = databaseService

  protected def init() {
    databaseService = (new GraphDatabaseFactory()
      .newEmbeddedDatabaseBuilder(filepath.getAbsolutePath)
      .setConfig(GraphDatabaseSettings.keep_logical_logs, "100M size")
      .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
      .newGraphDatabase());

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
      tx.close()
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

  // FIXME: identify root node!
  def rootNode: T =
    Node.wrapNeoNode(service.getNodeById(0), this, rootCompanion)

  def getUnknownNode(id: Long): Node =
    Node.wrapNeoNode(service.getNodeById(id), this).get

  def getNode[S <: Node](id: Long, companion: NodeCompanion[S]): S =
    Node.wrapNeoNode(service.getNodeById(id), this, companion)
}
