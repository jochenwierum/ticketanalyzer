import de.jowisoftware.mining.importer.svn.SVNImporter
import de.jowisoftware.mining.importer.trac.TracImporter
import de.jowisoftware.mining.importer.AsyncDatabaseImportHandler
import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.neo4j.{Database, DBWithTransaction}
import de.jowisoftware.mining.importer.ConsoleProgressReporter

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
    val importer = new TracImporter()
    importer.url = "http://jowisoftware.de/trac/ssh/login/xmlrpc"
    importer.username = "test"
    importer.password = "test"
    importer.repositoryName = "trac1"
    importer
  }
  
  def importSVN(db: DBWithTransaction[RootNode]) = {
    val importer = new SVNImporter()
    importer.url = "https://test@jowisoftware.de:4443/svn/ssh"
    importer.repositoryName = "svn1"
    importer
  }
}