package de.jowisoftware.mining.test

import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Relationship

import scala.collection.JavaConversions._

import org.mockito.Mockito._

class RelationMockBuilder private[test] (sourceNode: Node, sinkNode: Node, name: String)(implicit context: MockContext) {
  val relationship = context.mock[Relationship](name)

  private[test] def finishMock(): Relationship = {
    when(relationship.getStartNode).thenReturn(sourceNode)
    when(relationship.getEndNode).thenReturn(sinkNode)
    when(relationship.getOtherNode(sinkNode)).thenReturn(sourceNode)
    when(relationship.getOtherNode(sourceNode)).thenReturn(sinkNode)

    relationship
  }

  private[test] def finishMockIterable(): java.lang.Iterable[Relationship] =
    asJavaIterable(Iterable(finishMock()))
}