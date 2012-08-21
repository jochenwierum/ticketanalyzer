package de.jowisoftware.mining.analyzer.reviews

import de.jowisoftware.mining.UserOptions

class ReviewOptions extends UserOptions("analyzer.review") {
  protected val defaultResult = Map[String, String](
    "nonDevs" -> "example1, example2")
  protected val htmlDescription = """<b>Analyze your Team-Reviews</b>"""

  def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Ignore these (non-)developers", text("nonDevs"))
  }
}