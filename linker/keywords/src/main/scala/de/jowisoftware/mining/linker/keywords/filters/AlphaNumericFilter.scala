package de.jowisoftware.mining.linker.keywords.filters

object AlphaNumericFilter {
  private val matcher = """\d+\w+|\w+\d+""".r
}

class AlphaNumericFilter extends Filter {
  def apply(word: String): FilterResult.Value =
    AlphaNumericFilter.matcher.findFirstIn(word) match {
      case Some(_) => FilterResult.Reject
      case None => FilterResult.Undecide
    }
}