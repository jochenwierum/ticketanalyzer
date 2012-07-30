package de.jowisoftware.mining.analyzer.data

import scala.Array.canBuildFrom

class TextMatrix(xtitles: Seq[String], ytitles: Seq[String]) {
  private val cells: Array[Array[Double]] = Array.fill(ytitles.size, xtitles.size)(0)

  def columnTitles = xtitles.toArray
  def rowTitles = ytitles.toArray

  def rows = Array.tabulate(ytitles.size, xtitles.size)((y, x) => cells(y)(x))

  def normalizedRows = {
    cells.map{ row =>
      val sum = row.sum
      row.map { _ / sum}
    }
  }

  def set(xTitle: String, yTitle: String, value: Double) {
    val xPos = xtitles.indexWhere(_ == xTitle)
    require(xPos >= 0, "illegal x: "+xTitle)
    val yPos = ytitles.indexWhere(_ == yTitle)
    require(yPos >= 0, "illegal y: "+yTitle)

    cells(yPos)(xPos) = value
  }
}