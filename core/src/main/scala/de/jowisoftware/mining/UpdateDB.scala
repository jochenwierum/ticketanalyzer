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

object UpdateDB {
  def main(args: Array[String]) {
    val settings = new Settings("config.properties")
    val dbPath = new File(AppUtil.basePath, settings.getString("db")).getCanonicalFile
    val db = new EmbeddedDatabase(dbPath, RootNode)

    val count = GlobalGraphOperations.at(db.service).getAllNodes.size
    var i = 0L
    var transaction = db.service.beginTx
    for (node <- GlobalGraphOperations.at(db.service).getAllNodes) {
      Node.neoNode2Node(node, db) // TODO: check relationships also!

      i = i + 1
      if (i % 1000 == 0) {
        println((100.0*i/count).formatted("%.2f")+" %: "+i+" of "+count+" Nodes...")
        transaction.success
        transaction.finish
        transaction = db.service.beginTx
      }
    }
    transaction.success
    transaction.finish
    println("Update finished")
  }
}