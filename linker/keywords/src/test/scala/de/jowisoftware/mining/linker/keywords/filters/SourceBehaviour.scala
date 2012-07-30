package de.jowisoftware.mining.linker.keywords.filters

import org.scalatest.FlatSpec

trait SourceBehaviour { this: AbstractFilterTest =>
  protected def nonConsumeableFilter(word: String, result: FilterResult.Value) {
    it should "not consume its source" in {
      val filter = newFilter
      filter.apply(word) should equal(result)
      filter.apply(word) should equal(result)
    }
  }
}