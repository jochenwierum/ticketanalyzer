package de.jowisoftware.mining.linker.keywords.filters

object AbbrevFilter extends AbstractRegexFilter("[A-Z][A-Z]".r,
  FilterResult.Accept, FilterResult.Undecide) {

  override def apply(s: String): FilterResult.Value = s.toLowerCase match {
    case "not" => FilterResult.Reject
    case "and" => FilterResult.Reject
    case "or" => FilterResult.Reject
    case _ => super.apply(s)
  }
}
