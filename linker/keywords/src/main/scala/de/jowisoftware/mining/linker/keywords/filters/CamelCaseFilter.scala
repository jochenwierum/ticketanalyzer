package de.jowisoftware.mining.linker.keywords.filters

object CamelCaseFilter extends AbstractRegexFilter("[a-z0-9][A-Z]".r,
  FilterResult.Accept, FilterResult.Undecide)
