package de.jowisoftware.mining.linker.keywords.filters

object AlphaNumericFilter extends AbstractRegexFilter("""\d+\w+|\w+\d+""".r,
  FilterResult.Reject, FilterResult.Undecide)
