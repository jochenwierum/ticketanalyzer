package de.jowisoftware.mining.analyzer.truck

import de.jowisoftware.mining.UserOptions

class TruckAnalyzerOptions extends UserOptions {
  protected val defaultResult = Map[String, String](
    "algorithm" -> "Ciritcal keywords")

  protected val htmlDescription = "<b>Truck Number</b><br />How critical is knowledge?"

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Algorithm", combobox("algorithm", TruckAnalyzer.queryMap.keys.toSeq))
  }
}