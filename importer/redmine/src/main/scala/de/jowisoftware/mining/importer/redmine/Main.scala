package de.jowisoftware.mining.importer.redmine

import de.jowisoftware.mining.importer._

object Main {
  object eventHandler extends ImportEvents {
    def loadedCommit(repository: String, commit: CommitData) {}
    def loadedTicket(repository: String, versions: List[TicketData], comments: Seq[TicketCommentData]) =
      println("Loaded ticket "+versions+", with comments "+comments)
    def countedCommits(count: Long) = println("Countet tickets: "+count)
    def countedTickets(count: Long) {}
    def finish() {}
  }

  def main(args: Array[String]) {
    val config = Map(
      "url" -> "http://jowisoftware.de:3000/",
      "key" -> "2ae8befe0e72f5cc5c3f0e8f364fe1c34ee340b5",
      "repositoryname" -> "default",
      "project" -> "1")

    new RedmineImporter(config, eventHandler).run()
  }
}