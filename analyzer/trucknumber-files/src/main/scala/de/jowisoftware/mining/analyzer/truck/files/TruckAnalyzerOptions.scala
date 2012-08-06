package de.jowisoftware.mining.analyzer.truck.files

import de.jowisoftware.mining.UserOptions

class TruckAnalyzerOptions() extends UserOptions {
  protected val defaultResult = Map[String, String](
    "output" -> "raw",
    "limit" -> "50",
    "inactive" -> "example1, example2")

  protected val htmlDescription = "<b>Truck Number</b><br />How critical is knowledge?"

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Limit output to X lines", text("limit"))
    panel.add("Inactive developers", text("inactive"))
    panel.add("Output", combobox("output", Seq("interpreted", "raw")))
  }
}