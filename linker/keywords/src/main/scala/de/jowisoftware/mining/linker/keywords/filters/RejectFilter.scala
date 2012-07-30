package de.jowisoftware.mining.linker.keywords.filters

object RejectFilter extends Filter {
  def apply(word: String) = FilterResult.Reject
}