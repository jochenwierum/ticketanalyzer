package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.relationships.Contains
import org.neo4j.graphdb.Direction

import de.jowisoftware.neo4j.content.NodeCompanion
import helper._

object RootNode extends NodeCompanion[RootNode] {
  val graphVersion = 1
  def apply = new RootNode
}

class RootNode extends MiningNode {
  val version = 1
  def updateFrom(version: Int) = {}

  private def getCollection[T <: MiningNode](implicit nodeType: NodeCompanion[T]): T = {
    val node = getFirstNeighbor(Direction.OUTGOING, Contains.relationType, nodeType)

    node match {
      case Some(node) => node
      case _ =>
        val node = writableDb.createNode(nodeType)
        this.add(node, Contains)
        node
    }
  }

  override def initProperties = {
    state(0)
    graphVersion(RootNode.graphVersion)
  }

  def updateRequired = graphVersion() < RootNode.graphVersion
  def updateFinished = graphVersion(RootNode.graphVersion)

  val state = intProperty("state")
  val graphVersion = intProperty("graphVersion", 0)

  lazy val statusCollection = getCollection(StatusRepository)
  lazy val componentCollection = getCollection(ComponentRepository)
  lazy val versionCollection = getCollection(VersionRepository)
  lazy val typeCollection = getCollection(TypeRepository)
  lazy val milestoneCollection = getCollection(MilestoneRepository)
  lazy val ticketRepositoryCollection = getCollection(TicketRepositoryRepository)
  lazy val personCollection = getCollection(PersonRepository)
  lazy val commitRepositoryCollection = getCollection(CommitRepositoryRepository)
  lazy val tagCollection = getCollection(TagRepository)
  lazy val resolutionCollection = getCollection(ResolutionRepository)
  lazy val priorityCollection = getCollection(PriorityRepository)
  lazy val severityCollection = getCollection(SeverityRepository)
  lazy val reproducabilityCollection = getCollection(ReproducabilityRepository)
  lazy val keywordCollection = getCollection(KeywordRepository)
}
