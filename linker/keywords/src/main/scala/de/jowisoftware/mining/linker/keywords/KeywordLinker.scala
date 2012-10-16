package de.jowisoftware.mining.linker.keywords

import scala.collection.immutable.Map
import de.jowisoftware.mining.linker.LinkEvents
import de.jowisoftware.mining.linker.keywords.filters.{ RejectFilter, NumericFilter, MinLengthFilter, FilterChain, CamelCaseFilter, AlphaNumericFilter, AbbrevFilter }
import de.jowisoftware.mining.model.nodes.{ TicketRepository, Ticket, CommitRepository }
import de.jowisoftware.mining.linker.keywords.filters.WordListAcceptFilter
import java.io.File
import scala.swing.Dialog
import grizzled.slf4j.Logging
import scala.io.Source
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory
import de.jowisoftware.mining.linker.keywords.filters.UniversalRegexFilter
import scala.io.UTF8Codec
import scala.io.Codec
import de.jowisoftware.util.AppUtil
import de.jowisoftware.mining.linker.keywords.filters.Filter
import de.jowisoftware.mining.linker.keywords.filters.WordListRejectFilter

object KeywordLinker {
  private val splitRegex = """[,.?!;:]?([\s\t\n\r]+|$)"""
  private val forbidden = Set("", "not", "and", "or")
}

class KeywordLinker(
    tickets: TicketRepository, commits: CommitRepository,
    options: Map[String, String], events: LinkEvents) extends Logging {
  import KeywordLinker._

  if (isSet("filterShort")) {
    require(options("filterShortMinLength").matches("""\d+"""))
    require(options("filterShortMinLength").toInt > 0)
  }

  private def isSet(key: String) = options.getOrElse(key, "false").toLowerCase == "true"

  private val lucene = new LuceneKeywordExtractor(options("language"))
  private val (preStemFilter, postStemFilter) = setupFilter(options)

  private def setupFilter(options: Map[String, String]) = {
    val preStemChain = new FilterChain
    val postStemChain = new FilterChain

    def addFilter(chain: FilterChain, filter: Option[Filter], warnName: String) = filter match {
      case Some(value) => chain.addFilter(value)
      case None =>
        error("Ignoring selected filter: settings file "+warnName+" does not exist")
    }

    if (isSet("filterUniversal")) {
      addFilter(preStemChain, AppUtil.withSettingsSource("linker-keywords-universal-prestem.txt")(new UniversalRegexFilter(_)), "linker-keywords-universal-prestem.txt")
      addFilter(postStemChain, AppUtil.withSettingsSource("linker-keywords-universal-poststem.txt")(new UniversalRegexFilter(_)), "linker-keywords-universal-poststem.txt")
    }
    if (isSet("filterShort")) postStemChain.addFilter(new MinLengthFilter(options("filterShortMinLength").toInt))
    if (isSet("filterNum")) preStemChain.addFilter(NumericFilter)
    if (isSet("filterAlphaNum")) preStemChain.addFilter(AlphaNumericFilter)
    if (isSet("filterAbbrevs")) preStemChain.addFilter(AbbrevFilter)
    if (isSet("filterCamelCase")) preStemChain.addFilter(CamelCaseFilter)

    if (isSet("filterWhitelist")) {
      addFilter(postStemChain, AppUtil.withSettingsSource("linker-keywords-whitelist.txt")(new WordListAcceptFilter(_)), "linker-keywords-whitelist.txt")
    }

    if (isSet("filterBlacklist")) {
      addFilter(postStemChain, AppUtil.withSettingsSource("linker-keywords-blacklist.txt")(new WordListRejectFilter(_)), "linker-keywords-blacklist.txt")
    }

    if (!isSet("filterAccept")) postStemChain.addFilter(RejectFilter)

    (preStemChain, postStemChain)
  }

  def link() {
    events.reportProgress(0, 1, "Counting tickets...")
    val size = tickets.tickets.size
    var i = 0

    for (ticket <- tickets.tickets) {
      val keywords = processTitle(ticket) ++
        processText(ticket) ++
        processTags(ticket) ++
        processComments(ticket)

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
    if (isSet("parseTags")) { cleanWords(ticket.tags map { _.name() } toSet) }
    else Set()

  private def processTitle(ticket: Ticket) =
    if (isSet("parseTitle")) createKeywordSet(ticket.title())
    else Set()

  private def processText(ticket: Ticket) =
    if (isSet("parseText")) createKeywordSet(ticket.text())
    else Set()

  private def processComments(ticket: Ticket) =
    if (isSet("parseComments"))
      ticket.comments.flatMap(comment => createKeywordSet(comment.text()))
    else
      Set()

  private def createKeywordSet(s: String): Set[String] = {
    val words = cleanWords(s split splitRegex map cleanChars toSet)

    val (acceptFirst, stemList) = preStemFilter(words)
    val stemmedWords = stemList map stem filterNot (_.isEmpty) map (_.replaceAll("""[\s\t\n\r]+""", "-"))
    val (acceptSecond, undecided) = postStemFilter(stemmedWords)

    acceptFirst ++ acceptSecond ++ undecided
  }

  private def stem(s: String): String = lucene.getKeyword(s)

  private def cleanChars(s: String): String = {
    var result = s

    if (result.matches("""[('"].*""")) {
      result = result.substring(1)
    }

    if (result.matches(""".*["']""")) {
      result = result.substring(0, result.length - 1)
    }

    if (result.matches("""[^(]*\)""")) {
      result = result.substring(0, result.length - 1)
      if (result.matches(""".*["']""")) {
        result = result.substring(0, result.length - 1)
      }
    }

    if (result.matches("""[A-Z]\w+""")) {
      result = result.toLowerCase
    }

    result
  }

  private def cleanWords(s: Set[String]): Set[String] =
    s filterNot (keyword => forbidden.contains(keyword.toLowerCase))
}