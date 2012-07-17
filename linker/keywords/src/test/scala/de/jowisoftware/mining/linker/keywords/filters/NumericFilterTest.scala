package de.jowisoftware.mining.linker.keywords.filters

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class NumericFilterTest extends AbstractFilterTest {
  protected def newFilter = new NumericFilter

  "A NumericFilter" should "not reject words without digits" in {
    check("hello", FilterResult.Undecide)
    check("world", FilterResult.Undecide)
  }

  it should "not reject alphanumeric words" in {
    check("a2", FilterResult.Undecide)
    check("2a", FilterResult.Undecide)
  }

  it should "reject numeric words" in {
    check("42", FilterResult.Reject)
    check("23", FilterResult.Reject)
  }
}