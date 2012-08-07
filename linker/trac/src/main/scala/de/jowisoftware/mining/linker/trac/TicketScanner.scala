package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.TicketLink
import scala.util.matching.Regex

private[trac] object TicketScanner {
  private def ticketRegexes =
    """#(\d+)(?=\W|$)""".r ::
      """ticket:(\d+)(?=\W|$)""".r ::
      """(?i)mantis:?\s*(\d+)(?=\W|$)""".r ::
      """^(?:\[\w+\])?\s*0*(\d+)""".r ::
      """\[(\d+)\]""".r ::
      Nil
}

private[trac] class TicketScanner {
  import TicketScanner._

  def scan(text: String) =
    ticketRegexes.flatMap(_.findAllIn(text).matchData.map { theMatch =>
      TicketLink(theMatch.group(1).toInt)
    }).toSet
}