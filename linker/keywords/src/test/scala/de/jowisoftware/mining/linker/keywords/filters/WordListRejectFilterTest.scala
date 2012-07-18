package de.jowisoftware.mining.linker.keywords.filters

import scala.io.Source

class WordListRejectFilterTest extends AbstractFilterTest with SourceBehaviour {
  protected def wordSorce = Source.fromString(
    """ok
      |allow
      |correct
      |.*X""".stripMargin)

  protected def newFilter = new WordListRejectFilter(wordSorce)

  "A WordListRejectFilter" should "reject words from the word source" in {
    check("ok", FilterResult.Reject)
    check("correct", FilterResult.Reject)
  }

  it should "ignore words which are not in the word source" in {
    check("error", FilterResult.Undecide)
    check("unknown", FilterResult.Undecide)
  }

  it should "support regexes" in {
    check("testX", FilterResult.Reject)
    check("Xa", FilterResult.Undecide)
    check("OK", FilterResult.Undecide)
  }

  it should behave like nonConsumeableFilter("ok", FilterResult.Reject)
}