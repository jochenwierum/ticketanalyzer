package de.jowisoftware.mining.linker.keywords.filters

import scala.io.Source
import java.net.URI

class WordListAcceptFilter(source: Source) extends AbstractSourceListFilter(source) {
  protected val matchResult = FilterResult.Accept
  protected val noMatchResult = FilterResult.Undecide
}