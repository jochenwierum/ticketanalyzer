package de.jowisoftware.neo4j.database

import java.io.File

import de.jowisoftware.mining.UpdateDB
import de.jowisoftware.neo4j.content.{Node, NodeCompanion}
import de.jowisoftware.neo4j.{CypherService, DBWithTransaction, Database}
import de.jowisoftware.util.FileUtils
import grizzled.slf4j.Logging
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseSettings

object AbstractEmbeddedDatabase {
  val defaultSettings: List[(org.neo4j.graphdb.config.Setting[_], String)] =
    (GraphDatabaseSettings.keep_logical_logs, "100M size") ::
      (GraphDatabaseSettings.allow_store_upgrade, "true") :: Nil
}

private[database] abstract class AbstractEmbeddedDatabase(filepath: File) extends Database with Logging {
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
      try {
        tx.close()
      } catch {
        case e: Exception =>
          error("Could not close transaction", e)
      }
    }
  }

  def startTransaction: DBWithTransaction = {
    val tx = service.beginTx()
    new AutonomousTransaction(this, tx, cypherService)
  }

  def deleteContent(): Unit = {
    shutdown()
    FileUtils.delTree(filepath)
    UpdateDB.initDb()
    internalInit()
  }

  private def addShutdownHook(): Unit = {
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() =
        shutdown()
    })
  }

  def getUnknownNode(id: Long): Node =
    Node.wrapNeoNode(service.getNodeById(id), this).get

  def getNode[S <: Node](id: Long, companion: NodeCompanion[S]): S =
    Node.wrapNeoNode(service.getNodeById(id), this, companion)
}
