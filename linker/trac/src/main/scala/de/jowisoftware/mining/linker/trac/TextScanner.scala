package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.ScmLink
import de.jowisoftware.mining.linker.TicketLink
import de.jowisoftware.mining.linker.LinkEvents
import de.jowisoftware.mining.model.Node

private[trac] class TextScanner {
  def scan(text: String, events: LinkEvents, node: Node): Unit = {
    new SvnScmScanner().scan(text).foreach(link => events.foundLink(node, link))
    new TicketScanner().scan(text).foreach(link => events.foundLink(node, link))
  }
}