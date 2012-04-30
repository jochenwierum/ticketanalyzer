package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.ScmLink
import de.jowisoftware.mining.linker.TicketLink

private[linker] class TextScanner {
  def scan(text: String): (List[ScmLink], List[TicketLink]) =
    (new SvnScmScanner().scan(text), new TicketScanner().scan(text))
}