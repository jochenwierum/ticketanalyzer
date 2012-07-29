package de.jowisoftware.mining.linker.trac

import org.scalatest.FunSpec
import de.jowisoftware.mining.linker.TicketLink
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class TicketScannerTest extends FlatSpec with ShouldMatchers {
  private def check(text: String, expected: Set[TicketLink]) {
    info("testing: '"+text+"'")
    val scanner = new TicketScanner()
    val result = scanner.scan(text)

    assert(result === expected)
  }

  private def links(ids: Int*) = ids.toSet.map { id: Int => TicketLink(id) }

  "A TicketScanner" should "should find #single tickets referenced by '#'" in {
    check("Implemented #1 and #4", links(1, 4))
  }

  it should "find single tickets referenced by 'ticket'" in {
    check("Implemented ticket:26 and parts of ticket:12!", links(12, 26))
  }

  it should "support mantis links (case insensitive with optional space)" in {
    check("Implemented Mantis:1, mantis:2, MantiS: 3 and Mantis: 4!", links(1, 2, 3, 4))
  }

  it should "not find a link when it contains characters" in {
    check("A text with #1test in it", Set())
  }
}