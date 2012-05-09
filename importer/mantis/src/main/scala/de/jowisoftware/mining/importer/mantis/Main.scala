package de.jowisoftware.mining.importer.mantis

import de.jowisoftware.mining.importer.ImportEvents
import de.jowisoftware.mining.importer.TicketData
import de.jowisoftware.mining.importer.CommitData

object Main {
  object events extends ImportEvents {
    def countedTickets(count: Long) = println("Counted: "+count)
    def countedCommits(count: Long) = sys.error("Should not be called")
    def loadedTicket(ticket: TicketData) = println("Import: "+ticket)
    def loadedCommit(commit: CommitData) = sys.error("Should not be called")
    def finish() = println("Done!")
  }

  def main(args: Array[String]) = {
    org.apache.log4j.BasicConfigurator.configure()
    new MantisImporter().importAll(
      Map(
        "url" -> "http://jowisoftware.de/mant/api/soap/mantisconnect.php",
        "username" -> "administrator",
        "password" -> "test",
        "project" -> "1",
        "repositoryname" -> "repo1"),
      events)
  }
}