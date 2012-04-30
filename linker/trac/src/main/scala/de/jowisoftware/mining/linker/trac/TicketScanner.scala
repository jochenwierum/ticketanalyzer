package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.TicketLink

class TicketScanner {
  private def ticketRegexes = List("""#(\d+)""".r, """ticket:(\d+)""".r)

  def scan(text: String) =
    ticketRegexes.flatMap(_.findAllIn(text).matchData.map { theMatch =>
      TicketLink(theMatch.group(1))
    })
}