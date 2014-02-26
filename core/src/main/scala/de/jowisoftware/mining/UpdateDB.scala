package de.jowisoftware.mining

import java.io.File
import de.jowisoftware.neo4j.database.EmbeddedDatabase
import de.jowisoftware.util.AppUtil
import de.jowisoftware.mining.settings.Settings
import de.jowisoftware.mining.model.nodes.RootNode
import org.neo4j.tooling.GlobalGraphOperations
import scala.collection.JavaConversions._
import de.jowisoftware.neo4j.content.Node
import org.neo4j.jmx.Kernel
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.{ Node => NeoNode, Relationship => NeoRelationship }
import de.jowisoftware.neo4j.content.Relationship
import sun.awt.EmbeddedFrame
import org.neo4j.graphdb.DynamicLabel
import org.neo4j.helpers.TimeUtil
import java.util.concurrent.TimeUnit
import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import de.jowisoftware.neo4j.content.IndexedNodeInfo
import scala.collection.mutable.TreeSet
import IndexedNodeInfo.Labels.Label
import de.jowisoftware.neo4j.DBWithTransaction

object UpdateDB {
  def main(args: Array[String]) {
    val dbPath = AppUtil.projectFile(AppUtil.appSettings.getString("db"))
    val db = new EmbeddedDatabase(dbPath)

    if (db.inTransaction(_.rootNode(RootNode).graphVersion()) < 4) {
      println("Updating index...")
      updateIndizes(db);
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
      t.rootNode(RootNode).updateFinished
      t.success
    }

    println("Update finished")
    db.shutdown
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
        transaction.success
        transaction = db.startTransaction
      }
    }
    transaction.success
  }

  def updateIndizes(embeddedDb: EmbeddedDatabase) {
    val indices = embeddedDb.inTransaction { db =>
      val result = IndexedNodeInfo.Labels.labels flatMap { label =>
        if (db.service.schema().getIndexes(label.label).iterator().hasNext()) {
          None
        } else {
          println(s"Creating index: ${label.label.name()} on ${label.indexProperty}")
          Some(db.service.schema.indexFor(label.label).on(label.indexProperty).create())
        }
      }
      db.success()
      result
    }

    embeddedDb.inTransaction { db =>
      indices foreach { index =>
        println(s"Waiting for index: ${index.getLabel().name()}")
        db.service.schema().awaitIndexOnline(index, 10, TimeUnit.MINUTES)
      }
    }
  }
}