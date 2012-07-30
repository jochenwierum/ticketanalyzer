package de.jowisoftware.mining.linker.keywords.filters

import scala.collection.mutable.ArrayBuffer
import scala.annotation.tailrec
import grizzled.slf4j.Logging

class FilterChain extends Logging {
  val filters = new ArrayBuffer[Filter]

  @tailrec private def checkWord(word: String, filterList: Iterator[Filter], default: Boolean): Boolean =
    if (filterList.isEmpty) {
      trace("Accepted '"+word+"' because no filter matched")
      default
    } else {
      val currentFilter = filterList.next
      currentFilter.apply(word) match {
        case FilterResult.Accept =>
          trace("Accepted '"+word+"' by "+currentFilter.getClass.getSimpleName)
          true
        case FilterResult.Reject =>
          trace("Rejected '"+word+"' by "+currentFilter.getClass.getSimpleName)
          false
        case FilterResult.Undecide => checkWord(word, filterList, default)
      }
    }

  def apply(words: Iterable[String], defaultValue: Boolean) =
    for {
      word <- words
      if checkWord(word, filters.iterator, defaultValue)
    } yield word

  def addFilter(filter: Filter) =
    filters += filter
}