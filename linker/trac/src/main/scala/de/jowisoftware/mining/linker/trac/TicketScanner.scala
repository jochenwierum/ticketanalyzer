package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.TicketLink

private[trac] object TicketScanner {
  private def ticketRegexes =
    """#(\d+)(?=\W|$)""".r ::
      """ticket:(\d{1,18})(?=\W|$)""".r ::
      """(?i)mantis:?\s*(\d{1,18})(?=\W|$)""".r ::
      """^(?:\[\w+\])?\s*0*(\d{1,18})""".r ::
      """\[(\d{1,18})\]""".r ::
      Nil
}

private[trac] class TicketScanner {
  import de.jowisoftware.mining.linker.trac.TicketScanner._

  def scan(text: String) =
    ticketRegexes.flatMap(_.findAllIn(text).matchData.map { theMatch =>
      TicketLink(theMatch.group(1).toLong)
    }).toSet
}
