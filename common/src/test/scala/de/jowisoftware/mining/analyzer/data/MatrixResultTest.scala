package de.jowisoftware.mining.analyzer.data

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import de.jowisoftware.mining.test.MiningTest
import de.jowisoftware.mining.analyzer.MatrixResult

class MatrixResultTest extends MiningTest {
  """A MatrixResult""" should "return the column names in the initial order" in {
    val matrix = new MatrixResult(Seq("a", "c", "b"), Seq(), false, "")
    (matrix.columnTitles) should equal(Array("a", "c", "b"))

    val matrix2 = new MatrixResult(Seq("test", "test2"), Seq(), false, "")
    (matrix2.columnTitles) should equal(Array("test", "test2"))
  }

  it should "also return the row names in the initial order" in {
    val matrix = new MatrixResult(Seq(), Seq("a", "c", "b"), false, "")
    (matrix.rowTitles) should equal(Array("a", "c", "b"))

    val matrix2 = new MatrixResult(Seq(), Seq("test", "test2"), false, "")
    (matrix2.rowTitles) should equal(Array("test", "test2"))
  }

  it should "return 0 as the default value for all cells" in {
    val matrix = new MatrixResult(Seq("a", "b"), Seq("x", "y"), false, "")
    matrix.rows.forall(_.forall(_ == 0)) should equal(true)
  }

  it should "save values at the given position" in {
    val matrix = new MatrixResult(Seq("b", "a"), Seq("x", "y", "z"), false, "")
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
    val matrix = new MatrixResult(Seq("a", "b"), Seq("a", "b"), false, "")
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
      new MatrixResult(Seq("x"), Seq("b"), false, "").set("a", "b", 2.0)
    }
    intercept[IllegalArgumentException] {
      new MatrixResult(Seq("c"), Seq("y"), false, "").set("c", "x", 3.0)
    }
  }

  it should "be able to add numbers" in {
    val matrix = new MatrixResult(Seq("a", "b"), Seq("x"), false, "")
    matrix.set("a", "x", 3)
    matrix.add("a", "x", 1)
    matrix.set("b", "x", 2)

    val values = matrix.rows
    (values(0)(0)) should equal(4)
    (values(0)(1)) should equal(2)

    matrix.set("a", "x", 1)
    val values2 = matrix.rows
    (values2(0)(0)) should equal(1)
  }
}