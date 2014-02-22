package de.jowisoftware.mining.linker.trac

import org.scalatest.FunSpec
import de.jowisoftware.mining.linker.TicketLink
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.mining.test.MiningTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

class TicketScannerTest extends MiningTest {
  private def check(text: String, expected: Set[TicketLink]) {
    info("testing: '"+text+"'")
    val scanner = new TicketScanner()
    val result = scanner.scan(text)

    assert(result === expected)
  }

  private def links(ids: Int*) = ids.toSet.map { id: Int => TicketLink(id) }

  "A TicketScanner" should "should find single tickets referenced by '#'" in {
    check("Implemented #1 and #4 and CR:#3", links(1, 4, 3))
    check("[DW#12] [BF #13] it works!", links(12, 13))
  }

  it should "find single tickets referenced by 'ticket'" in {
    check("Implemented ticket:26 and parts of ticket:12!", links(12, 26))
  }

  it should "support mantis links (case insensitive with optional space)" in {
    check("Implemented Mantis:1, mantis:2, MantiS: 3 and Mantis   4!", links(1, 2, 3, 4))
  }

  it should "support links in curly brackets" in {
    check("[1], [2], and [864]!", links(1, 2, 864))
  }

  it should "not find a link when it contains characters" in {
    check("A text with #1test in it", links())
  }

  it should "find a reference if the text starts with a number" in {
    check("1234: a test", links(1234))
    check("00246: a test", links(246))
    check("[TAG] 00123: a test", links(123))
  }
}
