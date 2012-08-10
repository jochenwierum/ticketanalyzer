package de.jowisoftware.mining.linker.keywords.filters

import scala.io.Source
import grizzled.slf4j.Logging
import java.util.regex.Pattern

class UniversalRegexFilter(private var wordlist: Source) extends Filter with Logging {
  private case class RegexContainer(regex: Pattern, value: FilterResult.Value)

  private def toRegex(s: String) = Pattern.compile("^"+(s.substring(1).trim)+"$")

  private val regexList = wordlist.getLines.flatMap(regex =>
    if (regex.startsWith("#") || regex.trim.isEmpty)
      None
    else if (regex.startsWith("+"))
      Some(RegexContainer(toRegex(regex), FilterResult.Accept))
    else if (regex.startsWith("-"))
      Some(RegexContainer(toRegex(regex), FilterResult.Reject))
    else {
      error("Invalid line in filter file: "+regex)
      None
    }).toList

  def apply(word: String): FilterResult.Value = {
    regexList.find {
      _.regex.matcher(word).find
    } match {
      case Some(result) => result.value
      case None => FilterResult.Undecide
    }
  }
}