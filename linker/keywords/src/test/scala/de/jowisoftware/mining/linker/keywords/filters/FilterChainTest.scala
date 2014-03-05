package de.jowisoftware.mining.linker.keywords.filters

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import de.jowisoftware.mining.test.MockHelper
import org.mockito.Mockito._
import de.jowisoftware.mining.test.MiningTest
import de.jowisoftware.mining.test.MockContext

class FilterChainTest extends MiningTest {
  "A filter Chain" should "return 'undecide' by default" in {
    val chain = new FilterChain()

    (chain(Set("1", "2", "3"))) should equal(Set(), Set("1", "2", "3"))
    (chain(Set("a", "b"))) should equal(Set(), Set("a", "b"))
  }

  it should "put words into the 'undecided' list when no filter matches" in
    withMocks { implicit context: MockContext =>
      val filter: Filter = context.mock[Filter]("filter")
      val chain = new FilterChain()
      when(filter.apply("x")).thenReturn(FilterResult.Undecide)
      chain.addFilter(filter)

      (chain(Set("x"))) should equal(Set(), Set("x"))
  }

  it should "accept word if a filter returns 'Accept'" in withMocks { implicit context: MockContext =>
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

      chain(list) should equal(list, Set())

      verify(filter1).apply("word1")
      verify(filter2).apply("word1")
      verify(filter1).apply("word2")
      verifyNoMoreInteractions(filter1, filter2, filter3)
  }

  it should "reject a word if a filter returns 'Reject'" in withMocks { implicit context: MockContext =>
      val filter1: Filter = context.mock[Filter]("filter1")
      val filter2: Filter = context.mock[Filter]("filter2")

      val chain = new FilterChain()
      val list = Set("word1", "word2")
      chain.addFilter(filter1)
      chain.addFilter(filter2)

      when(filter1.apply("word1")).thenReturn(FilterResult.Reject)
      when(filter1.apply("word2")).thenReturn(FilterResult.Reject)

      chain(list) should equal(Set(), Set())

      verify(filter1).apply("word1")
      verify(filter1).apply("word2")
      verifyNoMoreInteractions(filter1, filter2)
  }

  it should "sort word by a filter's result" in withMocks { context: MockContext =>
      val filter1: Filter = context.mock[Filter]("filter1")
      val filter2: Filter = context.mock[Filter]("filter2")

      val chain = new FilterChain()
      val list = Set("word1", "word2", "word3")
      chain.addFilter(filter1)
      chain.addFilter(filter2)

      when(filter1.apply("word1")).thenReturn(FilterResult.Undecide)
      when(filter2.apply("word1")).thenReturn(FilterResult.Undecide)
      when(filter1.apply("word2")).thenReturn(FilterResult.Undecide)
      when(filter2.apply("word2")).thenReturn(FilterResult.Reject)
      when(filter1.apply("word3")).thenReturn(FilterResult.Accept)

      chain(list) should equal(Set("word3"), Set("word1"))

      verify(filter1).apply("word1")
      verify(filter2).apply("word1")
      verify(filter1).apply("word2")
      verify(filter2).apply("word2")
      verify(filter1).apply("word3")
      verifyNoMoreInteractions(filter1, filter2)
  }
}