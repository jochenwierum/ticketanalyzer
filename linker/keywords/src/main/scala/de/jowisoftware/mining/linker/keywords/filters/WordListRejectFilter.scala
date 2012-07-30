package de.jowisoftware.mining.linker.keywords.filters

import scala.io.Source
import java.net.URI

class WordListRejectFilter(source: Source) extends AbstractSourceListFilter(source) {
  protected val matchResult = FilterResult.Reject
  protected val noMatchResult = FilterResult.Undecide
}