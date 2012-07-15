package de.jowisoftware.mining.linker.trac

import org.scalatest.FunSpec
import de.jowisoftware.mining.linker.ScmLink
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.test.{ MockHelper, DBMockBuilder }
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.mining.model.nodes.Commit
import org.mockito.Mockito._
import org.neo4j.graphdb.Node
import de.jowisoftware.mining.test.MockContext

object ScmScannerTest {
  val repositoryName = "git"
}

class ScmScannerTest extends FunSpec with MockHelper {
  import ScmScannerTest._

  private def realCheck(text: String, expected: Set[ScmLink], database: DBWithTransaction[RootNode], generator: RangeGenerator) {
    val scanner = new ScmScanner(generator)
    val repository = database.rootNode.commitRepositoryCollection.findOrCreateChild(repositoryName)
    val result = scanner.scan(text, repository)

    assert(result === expected)
  }

  private def check(text: String, expected: Set[ScmLink], ranges: List[(String, String)] = Nil) {
    withMocks { implicit context =>
      val builder = new DBMockBuilder
      val repository = builder.addCommitRepository(repositoryName, false)
      val index = builder.addNodeIndex("Commit")

      expected.foreach { link =>
        index.add("uid", repositoryName+"-"+link.ref, repository.addCommit(link.ref))
      }

      val database = builder.finishMock
      val generator = setupRangeMock(ranges, context, database)

      realCheck(text, expected, database, generator)
    }
  }

  private def checkWithStarLookups(text: String, expected: Set[ScmLink],
    lookups: Map[String, String],
    ranges: List[(String, String)] = Nil) {
    withMocks { implicit context =>
      val builder = new DBMockBuilder
      val repository = builder.addCommitRepository(repositoryName, true)
      val index = builder.addNodeIndex("Commit")
      lookups.foreach {
        case (lookup, result) =>
          if (result != null)
            index.add(lookup, repository.addCommit(result))
          else
            index.add(lookup, null.asInstanceOf[Node])
      }

      val database = builder.finishMock
      val generator = setupRangeMock(ranges, context, database)

      realCheck(text, expected, database, generator)
    }
  }

  private def setupRangeMock(ranges: List[(String, String)], context: MockContext, dbMock: DBWithTransaction[RootNode]): RangeGenerator = {
    val generator = context.mock[RangeGenerator]()
    for {
      (commitId1, commitId2) <- ranges
    } {
      val rootNode = dbMock.rootNode
      val collectionCollection = rootNode.commitRepositoryCollection
      val commitCollection = collectionCollection.findOrCreateChild(repositoryName)
      val commit1 = commitCollection.findCommit(commitId1).get
      val commit2 = commitCollection.findCommit(commitId2).get
      when(generator.findRange(commit1, commit2))
        .thenReturn(Set(commit1, commit2))
    }
    generator
  }

  private def link(id: String, path: String = null) = ScmLink(id, path = Option(path))

  describe("A SvnScmScanner") {
    it("should find r1 and r4") {
      check("Fixed r1 and r4", Set(link("1"), link("4")))
    }

    it("should find changeset:1 and [4]") {
      check("Fixed changeset:1 and [4]", Set(link("1"), link("4")))
    }

    it("should find restricted changeset:2/trunk and [27/tags/v1]") {
      check("Fixed changeset:2/trunk and [27/tags/v1]",
        Set(link("2", "/trunk"),
          link("27", "/tags/v1")))
    }

    it("should find the ranges r23:24 and r10:12") {
      check("A test with r10:12 and r23:24", Set(
        link("10"), link("12"), link("23"), link("24")),
        ("23", "24") :: ("10", "12") :: Nil)
    }

    it("should find the ranges log:@11:12 and [1:3]") {
      check("A test with log:@11:12 and [1:3]", Set(
        link("11"), link("12"), link("1"), link("3")),
        ("1", "3") :: ("11", "12") :: Nil)
    }

    it("should find restricted ranges log:/trunk@11:12 and [1:3/tags/v7]") {
      check("A test with log:/trunk@11:12 and [1:3/tags/v7]", Set(
        link("11", "/trunk"), link("12", "/trunk"),
        link("1", "/tags/v7"), link("3", "/tags/v7")),
        ("1", "3") :: ("11", "12") :: Nil)
    }

    it("sould work with alpha numerical commit ids") {
      val lookups: Map[String, String] = Map("uid:git-123abc*" -> "123abc123",
        "uid:git-abc1234*" -> "abc1234f", "uid:git-abc123*" -> null)

      checkWithStarLookups("Changeset:123abc, changeset:abc123 and [abc1234] are broken",
        Set(link("123abc123"), link("abc1234f")), lookups, Nil)
    }
  }
}