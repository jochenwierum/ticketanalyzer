package de.jowisoftware.mining.linker.keywords

import scala.collection.immutable.Map
import de.jowisoftware.mining.linker.LinkEvents
import de.jowisoftware.mining.linker.keywords.filters.{ RejectFilter, NumericFilter, MinLengthFilter, FilterChain, CamelCaseFilter, AlphaNumericFilter, AbbrevFilter }
import de.jowisoftware.mining.model.nodes.{ TicketRepository, Ticket, CommitRepository }
import de.jowisoftware.mining.linker.keywords.filters.WordListAcceptFilter
import de.jowisoftware.util.AppUtil
import java.io.File
import scala.swing.Dialog
import grizzled.slf4j.Logging
import scala.io.Source
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory
import de.jowisoftware.mining.linker.keywords.filters.UniversalRegexFilter

class KeywordLinker(
    tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents) extends Logging {

  private def isSet(key: String) = options.getOrElse(key, "false").toLowerCase == "true"

  private val lucene = new LuceneKeywordExtractor(options("language"))
  private val filter = setupFilter(options)

  private def setupFilter(options: Map[String, String]) = {
    val chain = new FilterChain
    if (isSet("filterUniversal")) {
      val file = new File(AppUtil.basePath, "settings/universallist.txt")
      if (file.isFile()) {
        chain.addFilter(new UniversalRegexFilter(Source.fromFile(file)))
      } else {
        error("Ignoring universal filter: file settings/universallist.txt not exist")
      }
    }
    if (isSet("filterShort")) chain.addFilter(new MinLengthFilter(3))
    if (isSet("filterNum")) chain.addFilter(NumericFilter)
    if (isSet("filterAlphaNum")) chain.addFilter(AlphaNumericFilter)
    if (isSet("filterAbbrevs")) chain.addFilter(AbbrevFilter)
    if (isSet("filterCamelCase")) chain.addFilter(CamelCaseFilter)
    if (isSet("filterWhitelist")) {
      val file = new File(AppUtil.basePath, "settings/keywordwhitelist.txt")
      if (file.isFile()) {
        chain.addFilter(new WordListAcceptFilter(Source.fromFile(file)))
      } else {
        error("Ignoring whitelist filter: file settings/keywordwhitelist.txt does not exist")
      }
    }
    if (isSet("filterBlacklist")) {
      val file = new File(AppUtil.basePath, "settings/keywordblacklist.txt")
      if (file.isFile()) {
        chain.addFilter(new WordListAcceptFilter(Source.fromFile(file)))
      } else {
        error("Ignoring blacklist filter: file settings/keywordblacklist.txt does not exist")
      }
    }
    if (!isSet("filterAccept")) chain.addFilter(RejectFilter)

    chain
  }

  def link() {
    events.reportProgress(0, 1, "Counting tickets...")
    val size = tickets.tickets.size
    var i = 0

    for (ticket <- tickets.tickets) {
      var keywords: Set[String] = Set()

      keywords ++= processTitle(ticket)
      keywords ++= processText(ticket)
      keywords ++= processTags(ticket)
      keywords ++= processComments(ticket)

      keywords -= ""
      trace("Keywords for "+ticket.uid()+": "+keywords)

      if (!keywords.isEmpty) {
        events.foundKeywords(ticket, keywords)
      }

      i += 1
      events.reportProgress(i, size, "Extracting keywords")
    }
  }

  private def processTitle(ticket: Ticket) =
    if (isSet("parseTitle")) { filter(lucene.getKeywords(ticket.title())) } else Seq()

  private def processText(ticket: Ticket) =
    if (isSet("parseText")) { filter(lucene.getKeywords(ticket.text())) } else Seq()

  private def processTags(ticket: Ticket) =
    if (isSet("parseTags")) { ticket.tags map { _.name() } } else Seq()

  private def processComments(ticket: Ticket) =
    if (isSet("parseComments")) {
      ticket.comments.flatMap(comment => filter(lucene.getKeywords(comment.text())))
    } else Seq()
}