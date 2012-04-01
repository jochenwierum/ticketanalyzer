import de.jowisoftware.mining.importer.trac.TracImporter
import de.jowisoftware.mining.importer.ImportEvents
import de.jowisoftware.mining.importer.trac.DBImporter
import de.jowisoftware.neo4j._
import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.mining.model.Initialization
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
    
    Initialization.initDB(db)
    
    try {
      db.inTransaction {
        trans: DBWithTransaction[RootNode] =>
          //importTrac(trans)
          importSVN(trans)
          trans.success
      }
    } finally {
      db.shutdown;
    }
  }
  
  def importTrac(db: DBWithTransaction[RootNode]) {
    val importer = new TracImporter()
    importer.url = "http://jowisoftware.de/trac/ssh/login/xmlrpc"
    importer.username = "test"
    importer.password = "test"
    
    importer.importAll(new DBImporter(db), "trac1")
  }
  
  def importSVN(db: DBWithTransaction[RootNode]) {
    val importer = new SVNImporter()
    importer.url = "https://test@jowisoftware.de:4443/svn/ssh"
    importer.importAll(new DBImporter(db), "svn1")
  }
}