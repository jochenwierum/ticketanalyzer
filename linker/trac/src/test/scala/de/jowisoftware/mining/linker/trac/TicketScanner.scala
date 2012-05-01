package de.jowisoftware.mining.linker.trac

import org.scalatest.FunSpec
import de.jowisoftware.mining.linker.TicketLink

class TicketScannerTest extends FunSpec {
  private def check(text: String, expected: Set[TicketLink]) {
    val scanner = new TicketScanner()
    val result = scanner.scan(text)

    assert(result === expected)
  }

  private def link(id: String) = TicketLink(id)

  describe("A TicketScanner") {
    it("should find #1 and #4") {
      check("Implemented #1 and #4", Set(link("1"), link("4")))
    }
  }

  describe("A TicketScanner") {
    it("should find ticket:12 and ticket:26") {
      check("Implemented ticket:26 and parts of ticket:12!", Set(link("12"), link("26")))
    }
  }
}