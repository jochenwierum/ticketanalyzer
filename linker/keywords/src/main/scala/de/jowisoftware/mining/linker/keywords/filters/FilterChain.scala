package de.jowisoftware.mining.linker.keywords.filters

import scala.collection.mutable.ArrayBuffer
import scala.annotation.tailrec
import grizzled.slf4j.Logging

class FilterChain extends Logging {
  val filters = new ArrayBuffer[Filter]

  @tailrec private def checkWord(word: String, filterList: Iterator[Filter]): Boolean =
    if (filterList.isEmpty) {
      trace("Accepted '"+word+"' because no filter matched")
      true
    } else {
      val currentFilter = filterList.next
      currentFilter.apply(word) match {
        case FilterResult.Accept =>
          trace("Accepted '"+word+"' by "+currentFilter.getClass.getSimpleName)
          true
        case FilterResult.Reject =>
          trace("Rejected '"+word+"' by "+currentFilter.getClass.getSimpleName)
          false
        case FilterResult.Undecide => checkWord(word, filterList)
      }
    }

  def apply(words: Iterable[String]) = {
    for {
      word <- words
      if checkWord(word, filters.iterator)
    } yield word
  }

  def addFilter(filter: Filter) {
    filters += filter
  }
}