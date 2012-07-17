package de.jowisoftware.mining.linker.keywords.filters

import scala.io.Source
import java.net.URI

class WordListFilter(source: Source) extends Filter {
  def isInFile(word: String): Boolean = {
    val lowerWord = word.toLowerCase
    source.getLines.find(lowerWord == _.toLowerCase).isDefined
  }

  def apply(word: String): FilterResult.Value =
    isInFile(word) match {
      case true => FilterResult.Accept
      case false => FilterResult.Undecide
    }
}