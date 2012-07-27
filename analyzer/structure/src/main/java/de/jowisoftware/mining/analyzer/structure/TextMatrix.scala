package de.jowisoftware.mining.analyzer.structure

class TextMatrix(titles: String*) {
  private val cells: Array[Array[Double]] = Array.fill(titles.size, titles.size)(0)

  def columns = titles.toArray
  def rows = titles.toArray

  def values = Array.tabulate(cells.size, cells.size)((x, y) => cells(x)(y))

  def normalizedValues = {
    cells.map{ row =>
      val sum = row.sum
      row.map { _ / sum}
    }
  }

  def set(fromTitle: String, toTitle: String, value: Double) {
    val xPos = titles.indexWhere(_ == fromTitle)
    require(xPos >= 0, "illegal from: "+fromTitle)
    val yPos = titles.indexWhere(_ == toTitle)
    require(yPos >= 0, "illegal to: "+toTitle)

    cells(xPos)(yPos) = value
  }
}