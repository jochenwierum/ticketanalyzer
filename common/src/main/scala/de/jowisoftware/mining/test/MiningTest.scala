package de.jowisoftware.mining.test

import org.junit.runner.RunWith
import org.scalatest.{ FlatSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.scalatest.junit.JUnitSuite
import org.scalatest.FlatSpecLike
import org.scalatest.fixture.TestDataFixture
import org.scalatest.fixture.Suite

@RunWith(classOf[JUnitRunner])
class MiningTest extends FlatSpec with MockHelper with Matchers