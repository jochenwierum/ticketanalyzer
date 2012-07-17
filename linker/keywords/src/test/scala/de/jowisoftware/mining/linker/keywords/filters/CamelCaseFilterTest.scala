package de.jowisoftware.mining.linker.keywords.filters

class CamelCaseFilterTest extends AbstractFilterTest {
  protected def newFilter = CamelCaseFilter

  "A CamelCaseFilter" should "accept camel case words" in {
    check("CamelCase", FilterResult.Accept)
    check("MyFirstTest", FilterResult.Accept)
    check("anoterExample", FilterResult.Accept)
  }

  it should "ignore non-camelcase words" in {
    check("normal", FilterResult.Undecide)
    check("myfirsttest", FilterResult.Undecide)
  }
}