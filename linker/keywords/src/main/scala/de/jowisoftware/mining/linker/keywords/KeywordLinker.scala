package de.jowisoftware.mining.linker.keywords

import scala.collection.immutable.Map

import de.jowisoftware.mining.linker.{Linker, LinkEvents}
import de.jowisoftware.mining.model.nodes.{TicketRepository, CommitRepository}
import de.jowisoftware.mining.UserOptions

class KeywordLinker extends Linker {
  def userOptions(): UserOptions = new KeywordLinkerOptions

  def link(tickets: TicketRepository, commits: CommitRepository, options: Map[String, String], events: LinkEvents) {
    def isSet(key: String) = options.getOrElse(key, "false").toLowerCase == "true"

    val lucene = new LuceneKeywordExtractor(options("language"))

    events.reportProgress(0, 1, "Counting tickets...")
    val size = tickets.tickets.size
    var i = 0

    for(ticket <- tickets.tickets) {
      var keywords: Set[String] = Set()

      if (isSet("parseTitle")) { keywords ++= lucene.getKeywords(ticket.title()) }
      if (isSet("parseText"))  { keywords ++= lucene.getKeywords(ticket.text()) }
      if (isSet("parseTags"))  { keywords ++= ticket.tags map {_.name()} }

      if (isSet("parseComments")) {
        for (comment <- ticket.comments) {
          keywords ++= lucene.getKeywords(comment.text())
        }
      }

      keywords -= ""
      if (!keywords.isEmpty) {
        events.foundKeywords(ticket, keywords)
      }

      i += 1
      events.reportProgress(i, size, "Extracting keywords")
    }
  }
}