package de.jowisoftware.mining.test

import org.mockito.Mockito._
import org.neo4j.graphdb.Node
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.NodeCompanion
import scala.collection.JavaConversions._
import org.mockito.stubbing.OngoingStubbing
import org.mockito.invocation.InvocationOnMock
import de.jowisoftware.neo4j.ReadWriteDatabase

object NodeMockBuilder {
  private var nextId = 1
}

class NodeMockBuilder[A <: MiningNode] private[test] (companion: NodeCompanion[A], name: String = "")(implicit context: MockContext) {
  import NodeMockBuilder._
  import context._

  private val wrapper = companion()
  private var properties: List[String] = Nil
  private var wrapperIsPrepared = false

  val mockedNode: Node = if (name != "")
    context.mock[Node](name)
  else
    context.mock[Node](wrapper.getClass.getSimpleName)

  when(mockedNode.hasProperty("_version")).thenReturn(true)
  when(mockedNode.getProperty("_version")).thenReturnSingle(Integer.valueOf(wrapper.version))

  when(mockedNode.hasProperty("_class")).thenReturn(true)
  when(mockedNode.getProperty("_class")).thenReturnSingle(wrapper.getClass.getName)

  when(mockedNode.getId).thenReturn(nextId)
  nextId += 1

  when(mockedNode.getPropertyKeys).thenAnswer { _: InvocationOnMock => properties }

  final private[test] def finishMock(db: ReadWriteDatabase[RootNode]): A = {
    if (!wrapperIsPrepared) {
      wrapperIsPrepared = true
      wrapper.initWith(mockedNode, db)
    }
    wrapper
  }

  def addReadOnlyAttribute(name: String, value: Object) {
    when(mockedNode.hasProperty(name)).thenReturn(true)
    when(mockedNode.getProperty(name)).thenReturnSingle(value)
    properties = name :: properties
  }
}