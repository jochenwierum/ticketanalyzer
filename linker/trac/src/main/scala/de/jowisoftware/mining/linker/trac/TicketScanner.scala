package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.TicketLink
import scala.util.matching.Regex

private[trac] object TicketScanner {
  private def ticketRegexes = List("""#(\d+)""".r, """ticket:(\d+)""".r,
    new Regex("""(?i)mantis:\s?(\d+)"""))
}

private[trac] class TicketScanner {
  import TicketScanner._

  def scan(text: String) =
    ticketRegexes.flatMap(_.findAllIn(text).matchData.map { theMatch =>
      TicketLink(theMatch.group(1).toInt)
    }).toSet
}