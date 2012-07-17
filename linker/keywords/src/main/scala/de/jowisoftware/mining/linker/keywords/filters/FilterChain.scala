package de.jowisoftware.mining.linker.keywords.filters

import scala.collection.mutable.ArrayBuffer
import scala.annotation.tailrec

class FilterChain {
  val filters = new ArrayBuffer[Filter]

  @tailrec private def checkWord(word: String, filterList: Iterator[Filter]): Boolean =
    if (filterList.isEmpty) {
      true
    } else {
      val currentFilter = filterList.next
      currentFilter.apply(word) match {
        case FilterResult.Accept => true
        case FilterResult.Reject => false
        case FilterResult.Undecide => checkWord(word, filterList)
      }
    }

  def accepts(words: Set[String]) = {
    for {
      word <- words
      if checkWord(word, filters.iterator)
    } yield word
  }

  def addFilter(filter: Filter) {
    filters += filter
  }
}