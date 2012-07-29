package de.jowisoftware.mining.analyzer.workflow

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class TextMatrixTest extends FlatSpec with ShouldMatchers {
  """A TextMatrix""" should "return the column names in the initial order" in {
    val matrix = new TextMatrix("a", "c", "b")
    (matrix.columns) should equal(Array("a", "c", "b"))

    val matrix2 = new TextMatrix("test", "test2")
    (matrix2.columns) should equal(Array("test", "test2"))
  }

  it should "also return the row names in the initial order" in {
    val matrix = new TextMatrix("a", "c", "b")
    (matrix.rows) should equal(Array("a", "c", "b"))

    val matrix2 = new TextMatrix("test", "test2")
    (matrix2.rows) should equal(Array("test", "test2"))
  }

  it should "return 0 as the default value for all cells" in {
    val matrix = new TextMatrix("a", "b")
    matrix.values.forall(_.forall(_ == 0)) should equal(true)
  }

  it should "save values at the given position" in {
    val matrix = new TextMatrix("b", "a")
    matrix.set("b", "a", 2)
    matrix.set("a", "a", 7)
    matrix.set("a", "b", -2)

    val values = matrix.values
    (values(0)(0)) should equal(0)
    (values(0)(1)) should equal(2)
    (values(1)(0)) should equal(-2)
    (values(1)(1)) should equal(7)
  }

  it should "be able to normalize the values" in {
    val matrix = new TextMatrix("a", "b")
    matrix.set("a", "a", 2.0)
    matrix.set("a", "b", 2.0)
    matrix.set("b", "a", 75)
    matrix.set("b", "b", 25)

    val values = matrix.normalizedValues
    (values(0)(1)) should equal(0.5)
    (values(0)(0)) should equal(0.5)
    (values(1)(0)) should equal(0.75)
    (values(1)(1)) should equal(0.25)
  }

  it should "throw errors if the referenced column or row does not exist" in {
    intercept[IllegalArgumentException] {
      new TextMatrix("a").set("a", "b", 2.0)
    }
    intercept[IllegalArgumentException] {
      new TextMatrix("c").set("c", "x", 3.0)
    }
  }
}