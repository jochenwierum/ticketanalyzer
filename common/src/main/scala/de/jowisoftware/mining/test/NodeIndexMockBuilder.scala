package de.jowisoftware.mining.test

import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.index.IndexHits

import org.mockito.Mockito._

class NodeIndexMockBuilder private[test] (name: String)(implicit context: MockContext) {
  private[test] val index = context.mock[Index[Node]]("index-"+name)

  private def prepareExpectation(name: String, result: Node, block: => IndexHits[Node]) {
    val hits = context.mock[IndexHits[Node]](name)
    when(block).thenReturn(hits)
    when(hits.getSingle).thenReturn(result)
  }

  def add(name: String, value: String, result: Node): Unit =
    prepareExpectation("indexHits-"+this.name+"-"+name+"-"+value, result, index.query(name, value))

  def add(name: String, value: String, result: NodeMockBuilder[_]): Unit =
    add(name, value, result.mockedNode)

  def add(query: String, result: Node): Unit =
    prepareExpectation("indexHits-"+name+"-"+query, result, index.query(query))

  def add(query: String, result: NodeMockBuilder[_]): Unit =
    add(query, result.mockedNode)
}