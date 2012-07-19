package de.jowisoftware.mining.analyzer.truck

import de.jowisoftware.mining.UserOptions

class TruckAnalyzerOptions extends UserOptions {
  protected var result = Map[String, String](
    "algorithm" -> "Ciritcal keywords")

  def getHtmlDescription() = "<b>Truck Number</b><br />How critical is knowledge?"

  def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Algorithm", combobox("algorithm", TruckAnalyzer.queryMap.keys.toSeq))
  }
}