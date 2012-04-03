import de.jowisoftware.mining.importer.trac.TracImporter
import de.jowisoftware.mining.importer.ImportEvents
import de.jowisoftware.mining.importer.trac.DBImporter
import de.jowisoftware.neo4j._
import de.jowisoftware.mining.model.RootNode
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.ISVNLogEntryHandler
import org.tmatesoft.svn.core.SVNLogEntry
import de.jowisoftware.mining.importer.svn.SVNImporter

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
    scala.actors.Scheduler.shutdown()
  }
  
  def importFull(db: DBWithTransaction[RootNode]) = {
    val importer = new DBImporter(db.rootNode, importSVN(db), importTrac(db))
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