package de.jowisoftware.mining.test

import scala.reflect.runtime.universe
import org.mockito.Mockito._
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.index.IndexManager
import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.neo4j.CypherService
import org.mockito.ArgumentCaptor
import org.mockito.invocation.InvocationOnMock

class DBMockBuilder(implicit context: MockContext) {
  import context._

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

    val argCapture = ArgumentCaptor.forClass(classOf[Function1[DBWithTransaction, Any]])
    when(dbMock.inTransaction(argCapture.capture())).thenAnswer { _: InvocationOnMock =>
      argCapture.getValue().apply(dbMock)
    }

    autoFinish.foreach(_.finishMock(dbMock))

    dbMock
  }
}