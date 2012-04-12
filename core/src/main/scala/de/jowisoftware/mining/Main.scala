package de.jowisoftware.mining

import de.jowisoftware.mining.importer.Importer
import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.neo4j.{Database, DBWithTransaction}
import de.jowisoftware.mining.importer.async.AsyncDatabaseImportHandler
import de.jowisoftware.mining.importer.async.ConsoleProgressReporter

object Main {
  def main(args: Array[String]) {
    val dbPath = "db/"
    Database.drop(dbPath)
    val db = Database(dbPath, RootNode)
    
    try {
      db.inTransaction {
        trans: DBWithTransaction[RootNode] =>
          importFull(trans)
          trans.success
      }
    } finally {
      db.shutdown;
    }
    
    println("done")
    scala.actors.Scheduler.shutdown()
  }
  
  def importFull(db: DBWithTransaction[RootNode]) = {
    val importer = new AsyncDatabaseImportHandler(db.rootNode, 
        importSVN(db), 
        importTrac(db)
        ) with ConsoleProgressReporter
    importer.run()
  }
  
  def importTrac(db: DBWithTransaction[RootNode]) = {
    val importer = Class.forName("de.jowisoftware.mining.importer.trac.TracImporter").newInstance()
    val config = Map(
        "url" -> "http://jowisoftware.de/trac/ssh/login/xmlrpc",
        "username" -> "test",
        "password" -> "test",
        "repositoryName" -> "trac1")
    importer.asInstanceOf[Importer] -> config
  }
  
  def importSVN(db: DBWithTransaction[RootNode]) = {
    val importer = Class.forName("de.jowisoftware.mining.importer.svn.SVNImporter").newInstance()
    val config = Map("url" -> "https://test@jowisoftware.de:4443/svn/ssh",
        "repositoryName" -> "svn1")
    importer.asInstanceOf[Importer] -> config
  }
}