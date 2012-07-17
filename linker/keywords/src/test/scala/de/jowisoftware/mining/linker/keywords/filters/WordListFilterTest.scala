package de.jowisoftware.mining.linker.keywords.filters

import scala.io.Source

class WordListFilterTest extends AbstractFilterTest {
  protected def wordSorce = Source.fromString(
    """ok
      |allow
      |correct""".stripMargin)

  protected def newFilter: Filter = new WordListFilter(wordSorce)

  "A WordListFilter" should "accept words from the word source" in {
    check("ok", FilterResult.Accept)
    check("coRRect", FilterResult.Accept)
  }

  it should "ignore words which are not in the word source" in {
    check("error", FilterResult.Undecide)
    check("unknown", FilterResult.Undecide)
  }
}