package de.jowisoftware.mining.test

import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Relationship

import scala.collection.JavaConversions._

import org.easymock.EasyMock._

class RelationMockBuilder private[test] (sourceNode: Node, sinkNode: Node, name: String)(implicit context: MockContext) {
  val relationship = context.mock[Relationship](name)

  private[test] def finishMock(): Relationship = {
    expect(relationship.getStartNode).andReturn(sourceNode).anyTimes
    expect(relationship.getEndNode).andReturn(sinkNode).anyTimes
    expect(relationship.getOtherNode(sinkNode)).andReturn(sourceNode).anyTimes
    expect(relationship.getOtherNode(sourceNode)).andReturn(sinkNode).anyTimes

    relationship
  }

  private[test] def finishMockIterable(): java.lang.Iterable[Relationship] =
    asJavaIterable(Iterable(finishMock()))
}