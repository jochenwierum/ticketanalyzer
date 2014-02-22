package de.jowisoftware.mining.test

import org.junit.runner.RunWith
import org.scalatest.{ FlatSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.scalatest.junit.JUnitSuite
import org.scalatest.FlatSpecLike

// This class cannot be a trait! Traits seem to be ignored by the runner
@RunWith(classOf[JUnitRunner])
class MiningTest extends FlatSpec with Matchers with MockHelper