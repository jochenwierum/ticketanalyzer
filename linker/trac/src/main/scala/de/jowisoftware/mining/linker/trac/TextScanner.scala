package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.LinkEvents
import de.jowisoftware.mining.model.nodes.CommitRepository
import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.DBWithTransaction

private[trac] class TextScanner {
  def scan(transaction: DBWithTransaction, text: String, events: LinkEvents, node: MiningNode, commitRepository: CommitRepository): Unit = {
    new ScmScanner(new Neo4jRangeGenerator(transaction))
      .scan(text, commitRepository)
      .foreach(link => events.foundLink(node, link))

    new TicketScanner()
      .scan(text)
      .foreach(link => events.foundLink(node, link))
  }
}
