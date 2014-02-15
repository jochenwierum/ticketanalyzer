package de.jowisoftware.mining.test

import scala.reflect.runtime.universe

import org.mockito.Mockito.when
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.index.IndexManager

import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.{DBWithTransaction, DatabaseCollection}
import de.jowisoftware.neo4j.content.IndexedNodeCompanion

class DBMockBuilder(implicit context: MockContext) {
  private var repositories: List[(IndexedNodeMockBuilder[_ <: MiningNode], String)] = /*new NodeMockBuilder(CommitRepository) ::*/ Nil
  private var nodeIndex: Map[String, NodeIndexMockBuilder] = Map()

  private val service = context.mock[GraphDatabaseService]("service")
  private val indexManager = context.mock[IndexManager]()

  def addCommitRepository(name: String, supportsAbbrev: Boolean) = {
    val builder = new CommitRepositoryMockBuilder(supportsAbbrev)
    builder.setName(name)
    repositories = (builder, name) :: repositories
    builder
  }

  def finishMock = {
    val dbMock = context.mock[DBWithTransaction]("dbWithTransaction")

    val service = createService
    val collections = createCollectionsNode(dbMock)
    when(dbMock.service).thenReturn(service)
    when(dbMock.collections).thenReturn(collections)

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

  private def createCollectionsNode(mockObject: DBWithTransaction) = {
    val collections = context.mock[DatabaseCollection]("collections")
    for ((collectionBuilder, name) <- repositories) {
      val mock = collectionBuilder.finishMock(mockObject)
      val companion = collectionBuilder.companion.asInstanceOf[IndexedNodeCompanion[MiningNode]]
      when(collections.findOrCreate(companion, name)).thenReturn(mock)
    }
    collections
  }
}