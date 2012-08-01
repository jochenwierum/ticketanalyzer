package de.jowisoftware.mining.linker.keywords.filters

class AbbrevFilterTest extends AbstractFilterTest {
  protected def newFilter = AbbrevFilter

  "A AbbrevFilter" should "accept any abbreviations (words with 2 uppercase letters)" in {
    check("REST", FilterResult.Accept)
    check("HTML5", FilterResult.Accept)
  }

  it should "not accept any non-abbreviations" in {
    check("rest", FilterResult.Undecide)
    check("test", FilterResult.Undecide)
  }
}