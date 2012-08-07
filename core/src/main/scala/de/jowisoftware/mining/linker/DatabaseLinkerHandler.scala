package de.jowisoftware.mining.linker

import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.database.AutonomousTransaction
import de.jowisoftware.mining.helper.AutoTransactions
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.model.relationships.Links
import grizzled.slf4j.Logging

class DatabaseLinkerHandler(protected val db: Database[RootNode],
  ticketRepositoryName: String, commitRepositoryName: String)
    extends LinkEvents with AutoTransactions with Logging {
  val transactionThreshould = 50

  def reportProgress(progress: Long, max: Long, message: String) {}
  def finish() {
    transaction.success
  }

  def foundKeywords(source: MiningNode, keywords: Set[String]) {
    for (keyword <- keywords) {
      val keywordNode = root.keywordCollection.findOrCreateChild(keyword)
      keywordNode.add(source, Links).linkType("keyword")
    }
    safePointReached
  }

  def foundLink(source: MiningNode, link: Link) {
    // we use a separate transaction here, so we have to re-lookup the node
    val sourceNode = transaction.getUnknownNode(source.id)

    debug("Found link from "+sourceNode.getClass.getSimpleName+" "+
      sourceNode.content.getProperty("uid")+" to "+link)

    val (destNodes, linkType) = (link match {
      case ScmLink(ref, linkType, _) =>
        (commitRepository.findCommit(ref).toSeq, linkType)
      case TicketLink(id, linkType) =>
        (ticketRepository.findAllVersionsOf(id), linkType)
    })

    if (destNodes.isEmpty) {
      error("Node "+sourceNode+" refers to unknown target "+link)
    } else {
      destNodes.foreach(node =>
        sourceNode.add(node, Links).linkType(linkType.toString))
      safePointReached
    }
  }

  def foundStatusMap(status: Status, foundType: StatusType.Value) {
    // we use a separate transaction here, so we have to re-lookup the node
    val statusNode = transaction.getUnknownNode(status.id).asInstanceOf[Status]
    statusNode.logicalType(Option(foundType.id))
    safePointReached
  }

  def ticketRepository: TicketRepository =
    root.ticketRepositoryCollection.findOrCreateChild(ticketRepositoryName)
  def commitRepository: CommitRepository =
    root.commitRepositoryCollection.findOrCreateChild(commitRepositoryName)
}