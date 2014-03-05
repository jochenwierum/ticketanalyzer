package de.jowisoftware.mining.test

import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import de.jowisoftware.mining.model.nodes.helper.MiningNode
import org.mockito.Mockito._
import de.jowisoftware.neo4j.DBWithTransaction
import org.mockito.invocation.InvocationOnMock

class IndexedNodeMockBuilder[A <: MiningNode] private[test]
    (override val companion: IndexedNodeCompanion[A], name: String, mockName: String = "")
    (implicit context: MockContext)
    extends NodeMockBuilder[A](companion, mockName) {

  import context._

  override def finishMock(db: DBWithTransaction): A = {
    val mock = super.finishMock(db)

    val query = s"MATCH (n:${companion.indexInfo.label.name()}) WHERE n.name = {value} RETURN n LIMIT 1"
    when(db.cypher(query, Map("value" -> name))).
      thenAnswer { _: InvocationOnMock =>
        context.executionResult(Map("n" -> mockedNode))
    }

    mock
  }
}