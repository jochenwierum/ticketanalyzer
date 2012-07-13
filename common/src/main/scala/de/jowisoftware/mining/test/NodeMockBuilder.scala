package de.jowisoftware.mining.test

import org.easymock.EasyMock._
import org.neo4j.graphdb.Node

import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.NodeCompanion

class NodeMockBuilder[A <: MiningNode] private[test] (companion: NodeCompanion[A], name: String = "")(implicit context: MockContext) {
  private val wrapper = companion()
  val mockedNode: Node = if (name != "")
    context.mock[Node](name)
  else
    context.mock[Node](wrapper.getClass.getSimpleName)

  final private[test] def finishMock(db: DBWithTransaction[RootNode]): A = {
    val node = finishMockNode()
    wrapper.initWith(node, db)
    wrapper
  }

  protected[test] def finishMockNode(): Node = {
    expect(mockedNode.hasProperty("_version")).andReturn(true).anyTimes
    expect(mockedNode.getProperty("_version")).andReturn(Integer.valueOf(wrapper.version)).anyTimes

    expect(mockedNode.hasProperty("_class")).andReturn(true).anyTimes
    expect(mockedNode.getProperty("_class")).andReturn(wrapper.getClass.getName).anyTimes

    context.replay(mockedNode)
    mockedNode
  }

  def addReadOnlyAttribute(name: String, value: Object) {
    expect(mockedNode.hasProperty(name)).andReturn(true).anyTimes
    expect(mockedNode.getProperty(name)).andReturn(value).anyTimes
  }
}