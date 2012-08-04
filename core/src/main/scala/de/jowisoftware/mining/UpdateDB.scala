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

object UpdateDB {
  def main(args: Array[String]) {
    val settings = new Settings("config.properties")
    val dbPath = new File(AppUtil.basePath, settings.getString("db")).getCanonicalFile
    val db = new EmbeddedDatabase(dbPath, RootNode)

    println("Updating nodes...")
    performUpgrade[NeoNode](db,
      GlobalGraphOperations.at(db.service).getAllNodes,
      n => Node.neoNode2Node(n, db))

    println("Updating relationships...")
    performUpgrade[NeoRelationship](db,
      GlobalGraphOperations.at(db.service).getAllRelationships,
      r => updateRelationship(r, db))

    println("Update finished")
    db.shutdown
  }

  private def performUpgrade[A](db: EmbeddedDatabase[RootNode],
    collector: => Iterable[A], updateAction: A => Unit): Unit = {

    val count = collector.size
    var i = 0L
    var transaction = db.service.beginTx
    for (obj <- collector) {
      updateAction(obj)

      i = i + 1
      if (i % 1000 == 0) {
        println((100.0 * i / count).formatted("%.2f")+" %: "+i+" of "+count+" Objects...")
        transaction.success
        transaction.finish
        transaction = db.service.beginTx
      }
    }
    transaction.success
    transaction.finish
  }

  private def updateRelationship(rel: NeoRelationship, db: EmbeddedDatabase[RootNode]) {
    val className = rel.getProperty("_class").asInstanceOf[String]
    val target = Class.forName(className).newInstance.asInstanceOf[Relationship]
    target.initWith(rel, db)
  }
}