import de.jowisoftware.mining.importer.trac.TracImporter
import de.jowisoftware.mining.importer.ImportEvents

object Main {
  object DebugOutput extends ImportEvents {
    def loadedTicket(ticket: Map[String, Any]): Unit = println(ticket)
  }
  
  def main(args: Array[String]) {
    new TracImporter().importAll(DebugOutput)
  }
}