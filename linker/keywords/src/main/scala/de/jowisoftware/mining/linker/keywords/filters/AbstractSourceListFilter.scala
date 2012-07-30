package de.jowisoftware.mining.linker.keywords.filters

import scala.io.Source

abstract class AbstractSourceListFilter(private var source: Source) extends Filter {
  protected val matchResult: FilterResult.Value
  protected val noMatchResult: FilterResult.Value

  def isInFile(word: String): Boolean = {
    source = source.reset
    source.getLines.find(w => word.matches(w)).isDefined
  }

  def apply(word: String): FilterResult.Value =
    isInFile(word) match {
      case true => matchResult
      case false => noMatchResult
    }
}