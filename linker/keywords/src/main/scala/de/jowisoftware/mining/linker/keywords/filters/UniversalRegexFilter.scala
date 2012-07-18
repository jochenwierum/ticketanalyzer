package de.jowisoftware.mining.linker.keywords.filters

import scala.io.Source
import scala.util.matching.Regex

import grizzled.slf4j.Logging

class UniversalRegexFilter(private var wordlist: Source) extends Filter with Logging {
  case class RegexContainer(regex: Regex, value: FilterResult.Value)

  val regexList = wordlist.getLines.flatMap(regex =>
    if (regex.startsWith("#"))
      None
    else if (regex.startsWith("+"))
      Some(RegexContainer(("^"+regex.toLowerCase.substring(1)+"$").r, FilterResult.Accept))
    else if (regex.startsWith("-"))
      Some(RegexContainer(("^"+regex.toLowerCase.substring(1).r+"$").r, FilterResult.Reject))
    else {
      error("Invalid line in filter file: "+regex)
      None
    }).toList

  def apply(word: String): FilterResult.Value = {
    val lowerCaseWord = word.toLowerCase

    regexList.find {
      _.regex.findFirstIn(lowerCaseWord).isDefined
    } match {
      case Some(result) => result.value
      case None => FilterResult.Undecide
    }
  }
}