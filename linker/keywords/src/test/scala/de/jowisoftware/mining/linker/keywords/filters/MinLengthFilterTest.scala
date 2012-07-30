package de.jowisoftware.mining.linker.keywords.filters

class MinLengthFilterTest extends AbstractFilterTest {
  def newFilter() = new MinLengthFilter(3)

  "A MinLengthFilter(3)" should "not reject words longer than 2 letters" in {
    check("abcd", FilterResult.Undecide)
    check("1234", FilterResult.Undecide)
    check("ASD", FilterResult.Undecide)
  }

  it should "reject words shorter than 3 letters" in {
    check("zB", FilterResult.Reject)
    check("x", FilterResult.Reject)
  }

  "A MinLengthFilter(2)" should "accept words longer than 2 letters" in {
    val filter = new MinLengthFilter(2)
    filter.apply("zB") should be(FilterResult.Undecide)
  }
}