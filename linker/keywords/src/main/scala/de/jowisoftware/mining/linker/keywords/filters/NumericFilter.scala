package de.jowisoftware.mining.linker.keywords.filters

object NumericFilter {
  private val matcher = """^\d+$""".r
}

class NumericFilter extends Filter {
  def apply(word: String): FilterResult.Value =
    NumericFilter.matcher.findFirstIn(word) match {
      case Some(_) => FilterResult.Reject
      case None => FilterResult.Undecide
    }
}