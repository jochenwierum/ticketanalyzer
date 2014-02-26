package de.jowisoftware.neo4j.database

import java.io.File
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.{ GraphDatabaseFactory, GraphDatabaseSettings }
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }
import de.jowisoftware.neo4j.content.{ Node, NodeCompanion }
import de.jowisoftware.util.FileUtils
import org.neo4j.graphdb.factory.GraphDatabaseBuilder
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.CypherService

object AbstractEmbeddedDatabase {
  val defaultSettings: List[(org.neo4j.graphdb.config.Setting[_], String)] =
    (GraphDatabaseSettings.keep_logical_logs, "100M size") ::
      (GraphDatabaseSettings.allow_store_upgrade, "true") :: Nil
}

private[database] abstract class AbstractEmbeddedDatabase(filepath: File) extends Database {
  protected var databaseService: GraphDatabaseService = _
  private var cypherService: CypherService = _

  addShutdownHook()
  internalInit()

  private def internalInit(): Unit = {
    databaseService = init()
    cypherService = new Cypher(databaseService)
  }

  def service = databaseService

  protected def init(): GraphDatabaseService
  def shutdown(): Unit

  def inTransaction[S](body: DBWithTransaction => S): S = {
    val tx = service.beginTx()
    val wrapper = new DefaultTransaction(this, tx, cypherService)

    try {
      body(wrapper)
    } finally {
      tx.close()
    }
  }

  def startTransaction: DBWithTransaction = {
    val tx = service.beginTx()
    new AutonomousTransaction(this, tx, cypherService)
  }

  def deleteContent = {
    shutdown()
    FileUtils.delTree(filepath)
    internalInit()
  }

  private def addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      override def run() =
        shutdown()
    })
  }

  def getUnknownNode(id: Long): Node =
    Node.wrapNeoNode(service.getNodeById(id), this).get

  def getNode[S <: Node](id: Long, companion: NodeCompanion[S]): S =
    Node.wrapNeoNode(service.getNodeById(id), this, companion)
}
