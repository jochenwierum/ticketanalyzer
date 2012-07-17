package de.jowisoftware.mining.linker.keywords.filters

import scala.util.matching.Regex

abstract class AbstractRegexFilter(regex: Regex,
  matchingResult: FilterResult.Value, nonMatchingResult: FilterResult.Value)
    extends Filter {

  def apply(word: String): FilterResult.Value =
    regex.findFirstIn(word) match {
      case Some(_) => matchingResult
      case None => nonMatchingResult
    }
}