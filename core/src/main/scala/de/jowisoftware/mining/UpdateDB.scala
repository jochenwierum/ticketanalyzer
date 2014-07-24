package de.jowisoftware.mining

import java.util.concurrent.TimeUnit

import de.jowisoftware.mining.model.nodes.{File => MiningFile, _}
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.{IndexedNodeCompanion, Node, Relationship}
import de.jowisoftware.neo4j.database.EmbeddedDatabase
import de.jowisoftware.util.AppUtil
import org.neo4j.graphdb.{DynamicLabel, Label, Node => NeoNode, Relationship => NeoRelationship}
import org.neo4j.tooling.GlobalGraphOperations

import scala.collection.JavaConversions._

object UpdateDB {
  def main(args: Array[String]): Unit = {
    val dbPath = AppUtil.projectFile(AppUtil.appSettings.getString("db"))
    val db = new EmbeddedDatabase(dbPath)

    if (db.inTransaction(_.rootNode(RootNode).graphVersion()) < 4) {
      println("Updating index...")
      updateIndizes(db)
    }

    println("Updating nodes...")
    performUpgrade[NeoNode](db,
      GlobalGraphOperations.at(db.service).getAllNodes,
      (n, t) => Node.wrapNeoNode(n, t))

    println("Updating relationships...")
    performUpgrade[NeoRelationship](db,
      GlobalGraphOperations.at(db.service).getAllRelationships,
      (r, t) => Relationship.wrapNeoRelationship(r, t))

    println("Updating schema version")
    db.inTransaction { t =>
      t.rootNode(RootNode).updateFinished()
      t.success()
    }

    println("Update finished")
    db.shutdown()
  }

  def initDb(): Unit = {
    val dbPath = AppUtil.projectFile(AppUtil.appSettings.getString("db"))
    val db = new EmbeddedDatabase(dbPath)

    println("Creating index...")
    updateIndizes(db)

    db.inTransaction { t =>
      t.rootNode(RootNode).updateFinished()
      t.success()
    }

    db.shutdown()
  }

  private def performUpgrade[A](db: EmbeddedDatabase,
    collector: => Iterable[A], updateAction: (A, DBWithTransaction) => Unit): Unit = {

    var i = 0L
    var transaction = db.startTransaction
    val count = collector.size
    for (obj <- collector) {
      updateAction(obj, transaction)

      i = i + 1
      if (i % 1000 == 0) {
        println((100.0 * i / count).formatted("%.2f")+" %: "+i+" of "+count+" Objects...")
        transaction.success()
        transaction = db.startTransaction
      }
    }
    transaction.success()
  }

  def updateIndizes(embeddedDb: EmbeddedDatabase): Unit = {
    val companions: List[_ <: IndexedNodeCompanion[_]] =
      Commit :: CommitRepository :: Component :: MiningFile ::
        Keyword :: Milestone :: Person :: Priority :: Reproducability :: Resolution ::
        Severity :: Status :: Tag :: Ticket :: TicketComment :: TicketRepository ::
        Type :: Version :: Nil

    val indices = embeddedDb.inTransaction { db =>
      val result = companions flatMap { companion =>
        companion.indexInfo.properties match {
          case head :: tail =>
            (createIndex(db, companion.indexInfo.label, primary = true, head) ::
              tail.map(createIndex(db, companion.indexInfo.label, false, _))).flatten
          case Nil => Nil
        }
      }
      db.success()
      result
    }

    embeddedDb.inTransaction { db =>
      indices foreach { index =>
        println(s"Waiting for index: ${index.getLabel.name()}")
        db.service.schema().awaitIndexOnline(index, 10, TimeUnit.MINUTES)
      }
    }
  }

  private def createIndex(db: DBWithTransaction, label: Label, primary: Boolean, property: String) = {
    val propertyLabel = if (primary) label else DynamicLabel.label(s"${label.name()}-$property")
    if (db.service.schema().getIndexes(propertyLabel).iterator().hasNext) {
      None
    } else {
      println(s"Creating index: ${propertyLabel.name()} on $property")
      Some(db.service.schema.indexFor(propertyLabel).on(property).create())
    }
  }
}
