package de.jowisoftware.mining.test

import org.jmock.Expectations.returnValue
import org.jmock.Mockery
import org.neo4j.graphdb.Node

import MockHelper.{ mock, expecting }
import de.jowisoftware.mining.model.nodes.{ RootNode, CommitRepository }
import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.NodeCompanion

class DBMockBuilder(implicit context: Mockery) {
  import MockHelper._

  private var repositories: List[SimpleMock[_]] = new SimpleMock(CommitRepository) :: Nil

  class SimpleMock[A <: MiningNode] private[DBMockBuilder] (companion: NodeCompanion[A], name: String = "node") {
    val node = mock[Node](name)

    private[DBMockBuilder] def createObject(db: DBWithTransaction[RootNode]) = {
      val wrapper = companion()

      expecting { e =>
        import e._

        allowing(node).hasProperty("_version")
        will(returnValue(wrapper.version))
      }

      wrapper.initWith(node, db)
      wrapper
    }
  }

  def addCommitRepository(name: String) = {
    val mockObject = new SimpleMock(CommitRepository)
    repositories = mockObject :: repositories
  }

  def createMock = {
    val mockObject = mock[DBWithTransaction[RootNode]]("dbWithTransaction")
    addRootNode(mockObject)
    mockObject
  }

  private def addRootNode(mockObject: DBWithTransaction[RootNode]) = {
    new SimpleMock(RootNode, "rootNode")
  }
}