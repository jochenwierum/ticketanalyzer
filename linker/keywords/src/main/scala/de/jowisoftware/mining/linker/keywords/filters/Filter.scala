package de.jowisoftware.mining.linker.keywords.filters

trait Filter {
  def apply(word: String): FilterResult.Value
}