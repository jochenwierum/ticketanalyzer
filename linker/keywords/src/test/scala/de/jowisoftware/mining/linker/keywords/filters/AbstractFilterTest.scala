package de.jowisoftware.mining.linker.keywords.filters

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import de.jowisoftware.mining.test.MiningTest

abstract class AbstractFilterTest extends MiningTest {
  protected def newFilter: Filter

  protected def check(input: String, expected: FilterResult.Value) {
    info("testing: '"+input+"'")
    newFilter.apply(input) should equal(expected)
  }
}