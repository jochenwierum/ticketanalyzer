import de.jowisoftware.mining.importer.trac.TracImporter
import de.jowisoftware.mining.importer.ImportEvents
import de.jowisoftware.mining.importer.trac.DBImporter
import de.jowisoftware.neo4j._
import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.mining.model.Initialization

object Main {
  object DebugOutput extends ImportEvents {
    def loadedTicket(ticket: Map[String, Any]): Unit = println(ticket)
  }
  
  def main(args: Array[String]) {
    val dbPath = "db/"
    Database.drop(dbPath)
    val db = Database(dbPath, RootNode)
    
    Initialization.initDB(db)
    
    try {
      db.inTransaction {
        trans: DBWithTransaction[RootNode] =>
          new TracImporter().importAll(new DBImporter(trans, "trac1"))
          trans.success
      }
    } finally {
      db.shutdown;
    }
  }
}