package de.jowisoftware.mining.test

import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.index.IndexHits

import org.easymock.EasyMock._

class NodeIndexMock private[test] (name: String)(implicit context: MockContext) {
  private[test] val index = context.mock[Index[Node]]("index-"+name)

  private def prepareExpectation(name: String, result: Node, block: => IndexHits[Node]) {
    val hits = context.mock[IndexHits[Node]](name)
    expect(block).andReturn(hits).anyTimes
    expect(hits.getSingle).andReturn(result).anyTimes
  }

  def add(name: String, value: String, result: Node) =
    prepareExpectation("indexHits-"+this.name+"-"+name+"-"+value, result, index.query(name, value))

  def add(query: String, result: Node) =
    prepareExpectation("indexHits-"+name+"-"+query, result, index.query(query))
}