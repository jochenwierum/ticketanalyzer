package de.jowisoftware.mining.linker.keywords.filters

object NumericFilter extends AbstractRegexFilter("""^\d+$""".r,
  FilterResult.Reject, FilterResult.Undecide)
