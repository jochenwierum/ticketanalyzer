package de.jowisoftware.mining.test

import org.neo4j.graphdb.Node
import de.jowisoftware.mining.model.nodes.{ RootNode, CommitRepository }
import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.NodeCompanion
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.mining.model.nodes.CommitRepositoryRepository
import scala.collection.JavaConversions._
import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.index.IndexManager
import org.neo4j.graphdb.index.IndexHits

import org.mockito.Mockito._

class DBMockBuilder(implicit context: MockContext) {
  private var repositories: List[NodeMockBuilder[_]] = new NodeMockBuilder(CommitRepository) :: Nil
  private var nodeIndex: Map[String, NodeIndexMockBuilder] = Map()

  private val service = context.mock[AbstractGraphDatabase]("service")
  private val indexManager = context.mock[IndexManager]()

  def addCommitRepository(name: String, supportsAbbrev: Boolean) = {
    val builder = new CommitRepositoryMockBuilder(supportsAbbrev)
    builder.addReadOnlyAttribute("name", name)
    repositories = builder :: repositories
    builder
  }

  def finishMock = {
    val dbMock = context.mock[DBWithTransaction[RootNode]]("dbWithTransaction")

    val service = createService
    val rootNode = createRootNode(dbMock)
    when(dbMock.rootNode).thenReturn(rootNode)
    when(dbMock.service).thenReturn(service)

    dbMock
  }

  private def createService() = {
    when(service.index).thenReturn(indexManager)
    service
  }

  def addNodeIndex(name: String) = {
    val index = new NodeIndexMockBuilder(name)
    when(indexManager.forNodes(name)).thenReturn(index.index)
    index
  }

  private def createRootNode(mockObject: DBWithTransaction[RootNode]) = {
    val builder = new NodeMockBuilder(RootNode, "rootNode")

    addRepositoryNodes(builder.mockedNode, mockObject)
    builder.finishMock(mockObject)
  }

  private def addRepositoryNodes(rootNodeMock: Node, db: DBWithTransaction[RootNode]) = {
    val repositoryCollection = new NodeMockBuilder(CommitRepositoryRepository)

    val relationshipsToRepositories = repositories.map(repository =>
      new RelationMockBuilder(repositoryCollection.mockedNode, repository.mockedNode, "repositoryRelation").finishMock)

    when(repositoryCollection.mockedNode.getRelationships(Direction.OUTGOING, Contains.relationType))
      .thenReturn(relationshipsToRepositories)

    val repositoryCollectionRelation = new RelationMockBuilder(rootNodeMock,
      repositoryCollection.mockedNode, "repositoryCollectionRelation")

    val relationshipsToCommits = repositoryCollectionRelation.finishMockIterable
    when(rootNodeMock.getRelationships(Contains.relationType, Direction.OUTGOING))
      .thenReturn(relationshipsToCommits)

    repositoryCollection.finishMock(db)
  }
}