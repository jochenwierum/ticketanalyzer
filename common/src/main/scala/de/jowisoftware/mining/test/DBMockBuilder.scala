package de.jowisoftware.mining.test

import org.neo4j.graphdb.Node

import de.jowisoftware.mining.model.nodes.{ RootNode, CommitRepository }
import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.NodeCompanion

import org.easymock.EasyMock._

class DBMockBuilder(implicit context: MockHelper#MockContext) {
  private var repositories: List[NodeMockBuilder[_]] = new NodeMockBuilder(CommitRepository) :: Nil

  class NodeMockBuilder[A <: MiningNode] private[DBMockBuilder] (companion: NodeCompanion[A], name: String = "node") {
    val node = context.mock[Node](name)

    private[DBMockBuilder] def createMock(db: DBWithTransaction[RootNode]) = {
      val wrapper = companion()

      expect(node.hasProperty("_version")).andReturn(true)
      expect(node.getProperty("_version")).andReturn(Integer.valueOf(wrapper.version))

      expect(node.hasProperty("_class")).andReturn(true)
      expect(node.getProperty("_class")).andReturn(wrapper.getClass.getName)

      context.replay(node)
      wrapper.initWith(node, db)
      wrapper
    }
  }

  def addCommitRepository(name: String) = {
    val mockObject = new NodeMockBuilder(CommitRepository)
    repositories = mockObject :: repositories
  }

  def createMock = {
    val mockObject = context.mock[DBWithTransaction[RootNode]]("dbWithTransaction")
    val rootNode = addRootNode(mockObject)

    expect(mockObject.rootNode).andReturn(rootNode).anyTimes
    mockObject
  }

  private def addRootNode(mockObject: DBWithTransaction[RootNode]) =
    new NodeMockBuilder(RootNode, "rootNode").createMock(mockObject)
}