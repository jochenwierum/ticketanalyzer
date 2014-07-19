package de.jowisoftware.mining.test

import de.jowisoftware.mining.model.nodes.{Commit, CommitRepository}
import de.jowisoftware.neo4j.DBWithTransaction
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock

class CommitRepositoryMockBuilder private[test] (withAbbrev: Boolean, val name: String = "", val mockName: String = "")
  (implicit context: MockContext)
  extends IndexedNodeMockBuilder[CommitRepository](CommitRepository, name, mockName) {
  import context._

  private var commits: List[(NodeMockBuilder[Commit], String)] = Nil
  private var nonCommits: List[String] = Nil

  addReadOnlyAttribute("supportsAbbrev", withAbbrev: java.lang.Boolean)
  addReadOnlyAttribute("name", name)

  def addCommit(id: String) = {
    val builder = new NodeMockBuilder(Commit, "commit")
    builder.addReadOnlyAttribute("id", id)
    commits = (builder, id) :: commits

    builder
  }

  def addNoCommit(id: String): Unit = {
    nonCommits = id :: nonCommits
  }

  override def finishMock(db: DBWithTransaction): CommitRepository = {
    val mock = super.finishMock(db)

    def cypher(id: String): (String, String) = if (withAbbrev) {
      (s"MATCH (n:${Commit.indexInfo.label.name()}) WHERE n.uid =~ {value} RETURN n LIMIT 1", s"$name-$id.*")
    } else {
      (s"MATCH (n:${Commit.indexInfo.label.name()}) WHERE n.uid = {value} RETURN n LIMIT 1", s"$name-$id")
    }

    commits.foreach {
      case (commit, id) =>
        val (query, value) = cypher(id)
        when(db.cypher(query, Map("value" -> value))).thenAnswer { _: InvocationOnMock =>
          executionResult(Map("n" -> commit.mockedNode))
        }
        commit.finishMock(db)
    }

    nonCommits.foreach { id =>
      val (query, value) = cypher(id)
      when(db.cypher(query, Map("value" -> value))).thenAnswer { _: InvocationOnMock =>
        executionResult()
      }
    }

    mock
  }
}
