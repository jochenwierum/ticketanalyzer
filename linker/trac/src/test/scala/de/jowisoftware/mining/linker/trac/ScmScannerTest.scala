package de.jowisoftware.mining.linker.trac

import org.scalatest.FlatSpec
import de.jowisoftware.mining.linker.ScmLink
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.test.{ MockHelper, DBMockBuilder }
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.mining.model.nodes.Commit
import org.mockito.Mockito._
import org.neo4j.graphdb.Node
import de.jowisoftware.mining.test.MockContext
import org.scalatest.Matchers
import de.jowisoftware.mining.model.nodes.CommitRepository
import de.jowisoftware.mining.test.MiningTest

object ScmScannerTest {
  val repositoryName = "git"
}

class ScmScannerTest extends MiningTest {
  import ScmScannerTest._

  private def realCheck(text: String, expected: Set[ScmLink], database: DBWithTransaction, generator: RangeGenerator) {
    info("testing: '"+text+"'")

    val scanner = new ScmScanner(generator)
    val repository = CommitRepository.findOrCreate(database, repositoryName)
    val result = scanner.scan(text, repository)

    (result) should equal(expected)
  }

  private def check(text: String, expected: Set[ScmLink], ranges: List[(String, String)] = Nil) = withMocks { implicit context =>
    val builder = new DBMockBuilder
    val repository = builder.forCommitRepository(repositoryName, false)

    expected.foreach { link =>
      repository.addCommit(link.ref)
    }

    val database = builder.finishMock
    val generator = setupRangeMock(ranges, context, database)

    realCheck(text, expected, database, generator)
  }

  private def checkWithStarLookups(text: String, expected: Set[ScmLink],
      lookups: Map[String, Boolean],
      ranges: List[(String, String)] = Nil) = withMocks { implicit context =>
    val builder = new DBMockBuilder
    val repository = builder.forCommitRepository(repositoryName, true)

    for ((id, exists) <- lookups) {
      if (exists)
        repository.addCommit(id)
      else
        repository.addNoCommit(id)
    }

    val database = builder.finishMock
    val generator = setupRangeMock(ranges, context, database)

    realCheck(text, expected, database, generator)
  }

  private def setupRangeMock(ranges: List[(String, String)], context: MockContext, dbMock: DBWithTransaction): RangeGenerator = {
    val generator = context.mock[RangeGenerator]()
    for {
      (commitId1, commitId2) <- ranges
    } {

      val commitCollection = CommitRepository.findOrCreate(dbMock, repositoryName)
      val commit1 = commitCollection.findSingleCommit(commitId1).get
      val commit2 = commitCollection.findSingleCommit(commitId2).get
      when(generator.findRange(commit1, commit2)).thenReturn(Set(commit1, commit2))
    }
    generator
  }

  private def link(id: String, path: String = null) = ScmLink(id, path = Option(path))

  "A SvnScmScanner" should "find changesets starting with 'r'" in {
    check("Fixed r1 and r4", Set(link("1"), link("4")))
  }

  it should "find single changeset with 'changeset' and braces" in {
    check("Fixed changeset:1 and [4]", Set(link("1"), link("4")))
  }

  it should "find restricted single changesets (with pathes)" in {
    check("Fixed changeset:2/trunk and [27/tags/v1]",
      Set(link("2", "/trunk"),
        link("27", "/tags/v1")))
  }

  it should "find ranges starting with 'r'" in {
    check("A test with r10:12 and r23:24", Set(
      link("10"), link("12"), link("23"), link("24")),
      ("23", "24") :: ("10", "12") :: Nil)
  }

  it should "find ranges starting with 'log' and braces" in {
    check("A test with log:@11:12 and [1:3]", Set(
      link("11"), link("12"), link("1"), link("3")),
      ("1", "3") :: ("11", "12") :: Nil)
  }

  it should "find restricted ranges (with path names)" in {
    check("A test with log:/trunk@11:12 and [1:3/tags/v7]", Set(
      link("11", "/trunk"), link("12", "/trunk"),
      link("1", "/tags/v7"), link("3", "/tags/v7")),
      ("1", "3") :: ("11", "12") :: Nil)
  }

  it should "sould work with alphanumerical commit ids" in {
    val lookups: Map[String, Boolean] = Map("123abc" -> true,
      "abc1234" -> true, "abc123" -> false)

    checkWithStarLookups("Changeset:123abc, changeset:abc123 and [abc1234] are broken",
      Set(link("123abc"), link("abc1234")), lookups, Nil)
  }

  it should "not identify ranges with text as a link" in {
    check("a text with t2text log2:4test in it", Set(), Nil)
  }

  it should "not identify arrays as link" in {
    check("myArray[2]", Set(), Nil)
  }
}