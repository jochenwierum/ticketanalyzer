package de.jowisoftware.mining.analyzer.truck.tickets

import de.jowisoftware.mining.UserOptions
import scala.swing.Label

class TruckAnalyzerOptions() extends UserOptions {
  protected val defaultResult = Map[String, String](
    "output" -> "raw",
    "limit" -> "50",
    "inactive" -> "example1, example2",
    "filter-change-status" -> "false",
    "filter-reporter" -> "false",
    "filter-comment" -> "false",
    "filter-commit" -> "false",
    "filter-comment-commit" -> "false",
    "filter-status" -> "false")

  protected val htmlDescription = "<b>Truck Number</b><br />How critical is knowledge?"

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Limit output to X lines", text("limit"))
    panel.add("Inactive developers", text("inactive"))
    panel.add("Output", combobox("output", Seq("interpreted", "raw")))
    panel.addSpace
    panel.add("A person with knowledge must", new Label(""))
    panel.add("change the status", checkbox("filter-change-status", "At least one change is required"))
    panel.add("not be the reporter", checkbox("filter-reporter", "Ignore the reporter"))
    panel.add("wrote a comment", checkbox("filter-comment", "At least one comment is required"))
    panel.add("wrote a commit", checkbox("filter-commit", "At least one commit is required"))
    panel.add("wrote a comment or commit", checkbox("filter-comment-commit", "At least one comment OR commit is required"))
    panel.add("own the ticket in a non-new state", checkbox("filter-status", "The ticket must be processed by the person"))
  }
}