package de.jowisoftware.mining.linker.keywords.filters

object RejectMatcher extends Filter {
  def apply(word: String) = FilterResult.Reject
}