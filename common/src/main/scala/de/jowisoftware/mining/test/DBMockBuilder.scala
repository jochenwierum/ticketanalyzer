package de.jowisoftware.mining.test

import org.neo4j.graphdb.Node
import de.jowisoftware.mining.model.nodes.{ RootNode, CommitRepository }
import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.NodeCompanion
import org.easymock.EasyMock._
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.mining.model.nodes.CommitRepositoryRepository
import scala.collection.JavaConversions._
import org.neo4j.kernel.AbstractGraphDatabase
import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.index.IndexManager
import org.neo4j.graphdb.index.IndexHits

class DBMockBuilder(implicit context: MockContext) {
  private var repositories: List[NodeMockBuilder[_]] = new NodeMockBuilder(CommitRepository) :: Nil
  private var nodeIndex: Map[String, NodeIndexMock] = Map()

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

    expect(dbMock.rootNode).andReturn(createRootNode(dbMock)).anyTimes
    expect(dbMock.service).andReturn(createService).anyTimes

    dbMock
  }

  private def createService() = {
    expect(service.index).andReturn(indexManager).anyTimes
    service
  }

  def addNodeIndex(name: String) = {
    val index = new NodeIndexMock(name)
    expect(indexManager.forNodes(name)).andReturn(index.index).anyTimes
    index
  }

  private def createRootNode(mockObject: DBWithTransaction[RootNode]) = {
    val builder = new NodeMockBuilder(RootNode, "rootNode")

    addRepositoryNodes(builder.mockedNode, mockObject)
    builder.finishMock(mockObject)
  }

  private def addRepositoryNodes(rootNodeMock: Node, db: DBWithTransaction[RootNode]) = {
    val repositoryCollection = new NodeMockBuilder(CommitRepositoryRepository)

    val relations = repositories.map(repository =>
      new RelationMockBuilder(repositoryCollection.mockedNode, repository.finishMockNode, "repositoryRelation").finishMock)

    expect(repositoryCollection.mockedNode.getRelationships(Direction.OUTGOING, Contains.relationType))
      .andReturn(relations)
      .anyTimes()

    val repositoryCollectionRelation = new RelationMockBuilder(rootNodeMock,
      repositoryCollection.mockedNode, "repositoryCollectionRelation")

    expect(rootNodeMock.getRelationships(Contains.relationType, Direction.OUTGOING))
      .andReturn(repositoryCollectionRelation.finishMockIterable).anyTimes

    repositoryCollection.finishMock(db)
  }
}