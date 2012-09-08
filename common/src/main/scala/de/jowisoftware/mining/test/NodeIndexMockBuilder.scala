package de.jowisoftware.mining.test

import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.index.IndexHits
import scala.collection.JavaConversions.asJavaIterator

import org.mockito.Mockito._

class NodeIndexMockBuilder private[test] (name: String)(implicit context: MockContext) {
  private[test] val index = context.mock[Index[Node]]("index-"+name)

  private def prepareExpectation(name: String, result: Node, block: => IndexHits[Node]) {
    val hits = context.mock[IndexHits[Node]](name)
    when(block).thenReturn(hits)
    when(hits.getSingle).thenReturn(result)

    val tmp = when(hits.iterator)
    val methods = tmp.getClass.getMethods.filter(_.getName == "thenReturn")
    val method = methods.find(_.getParameterTypes.length == 1).get

    if (result == null) {
      method.invoke(tmp, asJavaIterator(Iterator()))
    } else {
      method.invoke(tmp, asJavaIterator(Iterator(result)))
    }
  }

  def add(name: String, value: String, result: Node): Unit =
    prepareExpectation("indexHits-"+this.name+"-"+name+"-"+value, result,
      index.query(name, value))

  def add(name: String, value: String, result: NodeMockBuilder[_]): Unit =
    add(name, value, result.mockedNode)

  def add(query: String, result: Node): Unit =
    prepareExpectation("indexHits-"+name+"-"+query, result, index.query(query))

  def add(query: String, result: NodeMockBuilder[_]): Unit =
    add(query, result.mockedNode)
}