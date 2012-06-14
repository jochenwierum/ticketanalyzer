package de.jowisoftware.mining.linker.trac

import org.scalatest.FunSpec
import de.jowisoftware.mining.linker.ScmLink

class SvnScmScannerTest extends FunSpec {
  private def check(text: String, expected: Set[ScmLink]) {
    val scanner = new SvnScmScanner()
    val result = scanner.scan(text)

    assert(result === expected)
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
  }
}