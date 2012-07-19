package de.jowisoftware.mining.linker.keywords.filters

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

abstract class AbstractFilterTest extends FlatSpec with ShouldMatchers {
  protected def newFilter: Filter

  protected def check(input: String, expected: FilterResult.Value) {
    info("testing: '"+input+"'")
    newFilter.apply(input) should equal(expected)
  }
}