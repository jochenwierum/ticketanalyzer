package de.jowisoftware.mining.linker.keywords.filters

object AbbrevFilter extends AbstractRegexFilter("[A-Z][A-Z]".r,
  FilterResult.Accept, FilterResult.Undecide) {
}
