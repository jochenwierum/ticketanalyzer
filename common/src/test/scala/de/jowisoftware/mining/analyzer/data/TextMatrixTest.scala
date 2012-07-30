package de.jowisoftware.mining.analyzer.data

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class TextMatrixTest extends FlatSpec with ShouldMatchers {
  """A TextMatrix""" should "return the column names in the initial order" in {
    val matrix = new TextMatrix(Seq("a", "c", "b"), Seq())
    (matrix.columnTitles) should equal(Array("a", "c", "b"))

    val matrix2 = new TextMatrix(Seq("test", "test2"), Seq())
    (matrix2.columnTitles) should equal(Array("test", "test2"))
  }

  it should "also return the row names in the initial order" in {
    val matrix = new TextMatrix(Seq(), Seq("a", "c", "b"))
    (matrix.rowTitles) should equal(Array("a", "c", "b"))

    val matrix2 = new TextMatrix(Seq(), Seq("test", "test2"))
    (matrix2.rowTitles) should equal(Array("test", "test2"))
  }

  it should "return 0 as the default value for all cells" in {
    val matrix = new TextMatrix(Seq("a", "b"), Seq("x", "y"))
    matrix.rows.forall(_.forall(_ == 0)) should equal(true)
  }

  it should "save values at the given position" in {
    val matrix = new TextMatrix(Seq("b", "a"), Seq("x", "y", "z"))
    matrix.set("b", "x", 2)
    matrix.set("a", "x", 7)
    matrix.set("a", "y", -2)
    matrix.set("a", "z", 9)
    matrix.set("b", "z", -8)

    val values = matrix.rows
    (values(0)(0)) should equal(2)
    (values(0)(1)) should equal(7)
    (values(1)(0)) should equal(0)
    (values(1)(1)) should equal(-2)
    (values(2)(0)) should equal(-8)
    (values(2)(1)) should equal(9)
  }

  it should "be able to normalize the values" in {
    val matrix = new TextMatrix(Seq("a", "b"), Seq("a", "b"))
    matrix.set("a", "a", 2.0)
    matrix.set("b", "a", 2.0)
    matrix.set("a", "b", 75)
    matrix.set("b", "b", 25)

    val values = matrix.normalizedRows
    (values(0)(1)) should equal(0.5)
    (values(0)(0)) should equal(0.5)
    (values(1)(0)) should equal(0.75)
    (values(1)(1)) should equal(0.25)
  }

  it should "throw errors if the referenced column or row does not exist" in {
    intercept[IllegalArgumentException] {
      new TextMatrix(Seq("x"), Seq("b")).set("a", "b", 2.0)
    }
    intercept[IllegalArgumentException] {
      new TextMatrix(Seq("c"), Seq("y")).set("c", "x", 3.0)
    }
  }
}