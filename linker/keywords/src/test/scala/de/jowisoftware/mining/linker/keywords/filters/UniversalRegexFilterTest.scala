package de.jowisoftware.mining.linker.keywords.filters

import scala.io.Source

class UniversalRegexFilterTest extends AbstractFilterTest with SourceBehaviour {
  protected def wordSource = Source.fromString(
    """+ok
      |+ allow
      |+.*X
      |-X.*
      |-deny
      |
      |#comment
      |error""".stripMargin)

  protected def newFilter = new UniversalRegexFilter(wordSource)

  "A UniversalRegexFilter" should "accept regexes which start with '+'" in {
    check("ok", FilterResult.Accept)
    check("testX", FilterResult.Accept)
    check("allow", FilterResult.Accept)
  }

  it should "reject regexes which start with '-'" in {
    check("deny", FilterResult.Reject)
    check("Xtest", FilterResult.Reject)
  }

  it should "ignore unknown words and comments" in {
    check("denyit", FilterResult.Undecide)
    check("error", FilterResult.Undecide)
    check("comment", FilterResult.Undecide)
    check("testx", FilterResult.Undecide)
  }

  it should behave like nonConsumeableFilter("ok", FilterResult.Accept)
}