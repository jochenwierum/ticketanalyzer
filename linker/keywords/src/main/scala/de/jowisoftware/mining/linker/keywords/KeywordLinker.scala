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

object KeywordLinker {
  private val splitRegex = """[.?;:]?(\s|$)"""
}

class KeywordLinker(
    tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents) extends Logging {
  import KeywordLinker._

  private def isSet(key: String) = options.getOrElse(key, "false").toLowerCase == "true"

  private val lucene = new LuceneKeywordExtractor(options("language"))
  private val (preStemFilter, postStemFilter) = setupFilter(options)

  private def setupFilter(options: Map[String, String]) = {
    val preStemChain = new FilterChain
    val postStemChain = new FilterChain

    if (isSet("filterUniversal")) {
      val preStemFile = new File(AppUtil.basePath, "settings/universallist-prestem.txt")
      val postStemFile = new File(AppUtil.basePath, "settings/universallist-poststem.txt")
      if (!preStemFile.isFile()) {
        error("Ignoring universal filter: file settings/universallist-prestem.txt not exist")
      }
      if (!postStemFile.isFile()) {
        error("Ignoring universal filter: file settings/universallist-prestem.txt not exist")
      }
      preStemChain.addFilter(new UniversalRegexFilter(Source.fromFile(preStemFile)))
      postStemChain.addFilter(new UniversalRegexFilter(Source.fromFile(postStemFile)))
    }
    if (isSet("filterShort")) postStemChain.addFilter(new MinLengthFilter(3))
    if (isSet("filterNum")) preStemChain.addFilter(NumericFilter)
    if (isSet("filterAlphaNum")) preStemChain.addFilter(AlphaNumericFilter)
    if (isSet("filterAbbrevs")) preStemChain.addFilter(AbbrevFilter)
    if (isSet("filterCamelCase")) preStemChain.addFilter(CamelCaseFilter)

    if (isSet("filterWhitelist")) {
      val file = new File(AppUtil.basePath, "settings/keywordwhitelist.txt")
      if (file.isFile()) {
        postStemChain.addFilter(new WordListAcceptFilter(Source.fromFile(file)))
      } else {
        error("Ignoring whitelist filter: file settings/keywordwhitelist.txt does not exist")
      }
    }
    if (isSet("filterBlacklist")) {
      val file = new File(AppUtil.basePath, "settings/keywordblacklist.txt")
      if (file.isFile()) {
        postStemChain.addFilter(new WordListAcceptFilter(Source.fromFile(file)))
      } else {
        error("Ignoring blacklist filter: file settings/keywordblacklist.txt does not exist")
      }
    }
    if (!isSet("filterAccept")) postStemChain.addFilter(RejectFilter)

    (preStemChain, postStemChain)
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

    events.finish
  }

  private def processTags(ticket: Ticket) =
    if (isSet("parseTags")) { ticket.tags map { _.name() } } else Seq()

  private def processTitle(ticket: Ticket) =
    if (isSet("parseTitle")) {
      preStemFilter(ticket.title().split(splitRegex), false) ++
        postStemFilter(lucene.getKeywords(ticket.title()), true)
    } else Seq()

  private def processText(ticket: Ticket) =
    if (isSet("parseText")) {
      preStemFilter(ticket.text().split(splitRegex), false) ++
        postStemFilter(lucene.getKeywords(ticket.text()), true)
    } else Seq()

  private def processComments(ticket: Ticket) =
    if (isSet("parseComments")) {
      ticket.comments.flatMap(comment =>
        preStemFilter(comment.text().split(splitRegex), false) ++
          postStemFilter(lucene.getKeywords(comment.text()), true))
    } else Seq()
}