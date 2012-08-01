package de.jowisoftware.mining.linker.keywords.filters

import scala.collection.mutable.ArrayBuffer
import scala.annotation.tailrec
import grizzled.slf4j.Logging

class FilterChain extends Logging {
  private val filters = new ArrayBuffer[Filter]

  @tailrec private def checkWord(word: String, filterList: Iterator[Filter]): FilterResult.Value =
    if (filterList.isEmpty) {
      trace("Skipping '"+word+"' because no filter matched")
      FilterResult.Undecide
    } else {
      val currentFilter = filterList.next
      currentFilter.apply(word) match {
        case FilterResult.Accept =>
          trace("Accepted '"+word+"' by "+currentFilter.getClass.getSimpleName)
          FilterResult.Accept
        case FilterResult.Reject =>
          trace("Rejected '"+word+"' by "+currentFilter.getClass.getSimpleName)
          FilterResult.Reject
        case FilterResult.Undecide => checkWord(word, filterList)
      }
    }

  def apply(words: Iterable[String]) = {
    var acceptList: Set[String] = Set()
    var undecideList: Set[String] = Set()

    for (word <- words) {
      checkWord(word, filters.iterator) match {
        case FilterResult.Undecide => undecideList += word
        case FilterResult.Accept => acceptList += word
        case FilterResult.Reject =>
      }
    }

    (acceptList, undecideList)
  }

  def addFilter(filter: Filter) =
    filters += filter
}