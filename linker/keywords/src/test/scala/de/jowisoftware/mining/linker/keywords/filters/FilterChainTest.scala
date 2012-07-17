package de.jowisoftware.mining.linker.keywords.filters

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import de.jowisoftware.mining.test.MockHelper

import org.mockito.Mockito._

class FilterChainTest extends FlatSpec with ShouldMatchers with MockHelper {
  "A filter Chain" should "accept by default" in {
    val chain = new FilterChain()

    (chain.accepts(Set("1", "2", "3"))) should equal(Set("1", "2", "3"))
    (chain.accepts(Set("a", "b"))) should equal(Set("a", "b"))
  }

  it should "accept when no filter matches" in {
    withMocks { context =>
      val filter: Filter = context.mock[Filter]("filter")
      val chain = new FilterChain()
      when(filter.apply("x")).thenReturn(FilterResult.Undecide)
      chain.addFilter(filter)

      (chain.accepts(Set("x"))) should equal(Set("x"))
    }
  }

  it should "accept word if a filter returns 'Accept'" in {
    withMocks { context =>
      val filter1: Filter = context.mock[Filter]("filter1")
      val filter2: Filter = context.mock[Filter]("filter2")
      val filter3: Filter = context.mock[Filter]("filter3")

      val chain = new FilterChain()
      val list = Set("word1", "word2")
      chain.addFilter(filter1)
      chain.addFilter(filter2)
      chain.addFilter(filter3)

      when(filter1.apply("word1")).thenReturn(FilterResult.Undecide)
      when(filter2.apply("word1")).thenReturn(FilterResult.Accept)
      when(filter1.apply("word2")).thenReturn(FilterResult.Accept)

      chain.accepts(list) should equal(list)

      verify(filter1).apply("word1")
      verify(filter2).apply("word1")
      verify(filter1).apply("word2")
      verifyNoMoreInteractions(filter1, filter2, filter3)
    }
  }

  it should "reject a word if a filter returns 'Reject'" in {
    withMocks { context =>
      val filter1: Filter = context.mock[Filter]("filter1")
      val filter2: Filter = context.mock[Filter]("filter2")

      val chain = new FilterChain()
      val list = Set("word1", "word2")
      chain.addFilter(filter1)
      chain.addFilter(filter2)

      when(filter1.apply("word1")).thenReturn(FilterResult.Reject)
      when(filter1.apply("word2")).thenReturn(FilterResult.Reject)

      chain.accepts(list) should equal(Set())

      verify(filter1).apply("word1")
      verify(filter1).apply("word2")
      verifyNoMoreInteractions(filter1, filter2)
    }
  }
}