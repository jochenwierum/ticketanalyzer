package de.jowisoftware.mining.linker.keywords.filters

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class AlphaNumericFilterTest extends AbstractFilterTest {
  protected def newFilter = AlphaNumericFilter

  "A AlphaNumericFilter" should "not reject words without digits" in {
    check("hello", FilterResult.Undecide)
    check("test", FilterResult.Undecide)
  }

  it should "reject alphanumerical words" in {
    check("aa2test", FilterResult.Reject)
    check("another2atest", FilterResult.Reject)
  }

  it should "reject numbers" in {
    check("42", FilterResult.Reject)
    check("23", FilterResult.Reject)
  }
}