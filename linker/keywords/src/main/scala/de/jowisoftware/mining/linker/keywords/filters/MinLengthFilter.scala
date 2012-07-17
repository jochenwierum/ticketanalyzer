package de.jowisoftware.mining.linker.keywords.filters

class MinLengthFilter(minLength: Int) extends Filter {
  def apply(word: String): FilterResult.Value =
    if (word.length < minLength)
      FilterResult.Reject
    else
      FilterResult.Undecide
}