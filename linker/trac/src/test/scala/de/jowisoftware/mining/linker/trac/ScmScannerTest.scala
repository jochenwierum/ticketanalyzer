package de.jowisoftware.mining.linker.trac

import org.scalatest.FunSpec

import de.jowisoftware.mining.linker.ScmLink
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.test.{ MockHelper, DBMockBuilder }
import de.jowisoftware.neo4j.DBWithTransaction

class ScmScannerTest extends FunSpec with MockHelper {
  private def check(text: String, expected: Set[ScmLink], database: DBWithTransaction[RootNode]) {
    val scanner = new ScmScanner()
    val repository = database.rootNode.commitRepositoryCollection.findOrCreateChild("git")
    val result = scanner.scan(text, repository)

    assert(result === expected)
  }

  private def check(text: String, expected: Set[ScmLink]) {
    prepareMock { implicit context =>
      val builder = new DBMockBuilder
      val repository = builder.addCommitRepository("git", false)
      val index = builder.addNodeIndex("Commit")
      expected.foreach { link =>
        index.add("uid", "git-"+link.ref, repository.addCommit(link.ref))
      }
      builder.finishMock
    } andCheck { database =>
      check(text, expected, database)
    }
  }

  private def link(id: String) = ScmLink(id, path = None)
  private def link(id: String, path: String) = ScmLink(id, path = Some(path))

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
        link("10"), link("11"), link("12"), link("23"), link("24")))
    }

    it("should find the ranges log:@11:12 and [1:3]") {
      check("A test with log:@11:12 and [1:3]", Set(
        link("11"), link("12"), link("1"), link("2"), link("3")))
    }

    it("should find restricted ranges log:/trunk@11:12 and [1:3/tags/v7]") {
      check("A test with log:/trunk@11:12 and [1:3/tags/v7]", Set(
        link("11", "/trunk"), link("12", "/trunk"),
        link("1", "/tags/v7"), link("2", "/tags/v7"), link("3", "/tags/v7")))
    }

    it("sould work with alpha numerical commit ids") {
      prepareMock { implicit context =>
        val builder = new DBMockBuilder
        val repository = builder.addCommitRepository("git", true)
        val index = builder.addNodeIndex("Commit")
        index.add("uid:git-123abc*", repository.addCommit("123abc123"))
        index.add("uid:git-abc1234*", repository.addCommit("abc1234f"))
        index.add("uid:git-abc123*", null)
        builder.finishMock
      } andCheck { database =>
        check("Changeset:123abc, changeset:abc123 and [abc1234] are broken",
          Set(link("123abc123"), link("abc1234f")), database)
      }
    }
  }
}