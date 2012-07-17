package de.jowisoftware.mining.linker.keywords.filters

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

abstract class AbstractFilterTest extends FlatSpec with ShouldMatchers {
  protected def newFilter: Filter

  protected def check(input: String, expected: FilterResult.Value) {
    info("testing: '"+input+"'")
    newFilter.apply(input) should equal(expected)
  }

  protected def nonConsumeableFilter(word: String, result: FilterResult.Value) {
    it should "not consume its source" in {
      val filter = newFilter
      filter.apply(word) should equal(result)
      filter.apply(word) should equal(result)
    }
  }
}