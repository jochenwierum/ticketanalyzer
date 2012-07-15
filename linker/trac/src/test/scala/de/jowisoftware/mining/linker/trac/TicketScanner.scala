package de.jowisoftware.mining.linker.trac

import org.scalatest.FunSpec
import de.jowisoftware.mining.linker.TicketLink

class TicketScannerTest extends FunSpec {
  private def check(text: String, expected: Set[TicketLink]) {
    val scanner = new TicketScanner()
    val result = scanner.scan(text)

    assert(result === expected)
  }

  private def links(ids: Int*) = ids.toSet.map { id: Int => TicketLink(id) }

  describe("A TicketScanner") {
    it("should find #1 and #4") {
      check("Implemented #1 and #4", links(1, 4))
    }

    it("should find ticket:12 and ticket:26") {
      check("Implemented ticket:26 and parts of ticket:12!", links(12, 26))
    }

    it("should support Mantis:12 and mantis: 14 (case insensitive with optional space)") {
      check("Implemented Mantis:1, mantis:2, MantiS: 3 and Mantis: 4!", links(1, 2, 3, 4))
    }

    it("should not find a link in #1test") {
      check("A text with #1test in it", Set())
    }
  }
}