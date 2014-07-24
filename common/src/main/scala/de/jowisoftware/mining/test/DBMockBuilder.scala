package de.jowisoftware.mining.test

import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import org.mockito.Mockito._
import org.neo4j.graphdb.GraphDatabaseService

class DBMockBuilder(implicit context: MockContext) {

  private var nodeIndex: Map[String, NodeIndexMockBuilder] = Map()
  private var autoFinish: List[IndexedNodeMockBuilder[_]] = Nil

  private val service = context.mock[GraphDatabaseService]("service")

  def forCommitRepository(name: String, supportsAbbrev: Boolean) = {
    val builder = new CommitRepositoryMockBuilder(supportsAbbrev, name)
    autoFinish = builder :: autoFinish
    builder
  }

  def forCompanion[A <: MiningNode](companion: IndexedNodeCompanion[A], name: String) = {
    val builder = new IndexedNodeMockBuilder[A](companion, name)
    autoFinish = builder :: autoFinish
    builder
  }

  def finishMock = {
    val dbMock = context.mock[DBWithTransaction]("dbWithTransaction")
    when(dbMock.service).thenReturn(service)
    autoFinish.foreach(_.finishMock(dbMock))

    dbMock
  }
}
