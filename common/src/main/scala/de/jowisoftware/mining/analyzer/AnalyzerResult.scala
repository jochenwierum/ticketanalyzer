package de.jowisoftware.mining.analyzer

import org.neo4j.cypher.ExecutionResult
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import java.awt.image.BufferedImage

abstract sealed class AnalyzerResult(val title: String) {
  var description = ""
}

class ImageResult(val image: BufferedImage, title: String) extends AnalyzerResult(title) {
  def this(bytes: Array[Byte], title: String) = this(ImageIO.read(new ByteArrayInputStream(bytes)), title)
}

class TextResult(val content: String, title: String) extends AnalyzerResult(title)

class NodeResult(val result: Iterator[_ <: Map[String, Any]], val columnOrder: Seq[String], val titles: Seq[String], title: String) extends AnalyzerResult(title) {
  def this(result: Iterator[_ <: Map[String, Any]], columnOrder: Seq[String], title: String) = this(result, columnOrder, columnOrder, title)
  def this(result: ExecutionResult, title: String) = this(result, result.columns, result.columns, title)
  def this(result: ExecutionResult, columnOrder: Seq[String], title: String) = this(result, result.columns, result.columns, title)
}

class MatrixResult(val xtitles: Seq[String], val ytitles: Seq[String], val highlight: Boolean, title: String) extends AnalyzerResult(title) {
  private val cells: Array[Array[Double]] = Array.fill(ytitles.size, xtitles.size)(0)

  def columnTitles = xtitles.toArray
  def rowTitles = ytitles.toArray

  def rows = Array.tabulate(ytitles.size, xtitles.size)((y, x) => cells(y)(x))

  def normalizedRows = {
    cells.map { row =>
      val sum = row.sum
      row.map { _ / sum }
    }
  }

  def set(xTitle: String, yTitle: String, value: Double) {
    val (xPos, yPos) = findCoords(xTitle, yTitle)
    cells(yPos)(xPos) = value
  }

  def add(xTitle: String, yTitle: String, value: Double) {
    val (xPos, yPos) = findCoords(xTitle, yTitle)
    cells(yPos)(xPos) += value
  }

  private def findCoords(xTitle: String, yTitle: String): (Int, Int) = {
    val xPos = xtitles.indexWhere(_ == xTitle)
    require(xPos >= 0, "illegal x: "+xTitle)
    val yPos = ytitles.indexWhere(_ == yTitle)
    require(yPos >= 0, "illegal y: "+yTitle)
    (xPos, yPos)
  }
}