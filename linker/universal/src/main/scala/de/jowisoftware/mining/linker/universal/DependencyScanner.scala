package de.jowisoftware.mining.linker.universal

import DependencyScanner.splitPos
import de.jowisoftware.mining.linker.{ TicketLink, LinkType, LinkEvents }
import de.jowisoftware.mining.model.Node

private[universal] object DependencyScanner {
  private val splitPos = """[, ]+"""
}

private[universal] class DependencyScanner {
  import DependencyScanner._

  def scan(text: String, events: LinkEvents, node: Node, linkType: LinkType.LinkType) {
    if (text.isEmpty) {
      return
    }

    val tickets = text.split(splitPos)
    tickets.foreach(ticket => events.foundLink(node, TicketLink(ticket, linkType = linkType)))
  }
}