package de.jowisoftware.mining.linker.keywords.filters

import scala.io.Source

class WordListAcceptFilterTest extends AbstractFilterTest with SourceBehaviour {
  protected def wordSorce = Source.fromString(
    """ok
      |allow
      |correct
      |.*X""".stripMargin)

  protected def newFilter = new WordListAcceptFilter(wordSorce)

  "A WordListAcceptFilter" should "accept words from the word source" in {
    check("ok", FilterResult.Accept)
    check("coRRect", FilterResult.Accept)
  }

  it should "ignore words which are not in the word source" in {
    check("error", FilterResult.Undecide)
    check("unknown", FilterResult.Undecide)
  }

  it should "support regexes" in {
    check("testX", FilterResult.Accept)
    check("Xa", FilterResult.Undecide)
  }

  it should behave like nonConsumeableFilter("ok", FilterResult.Accept)
}