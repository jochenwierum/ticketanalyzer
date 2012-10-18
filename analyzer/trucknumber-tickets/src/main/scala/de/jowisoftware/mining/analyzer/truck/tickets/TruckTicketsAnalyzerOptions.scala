package de.jowisoftware.mining.analyzer.truck.tickets

import de.jowisoftware.mining.UserOptions
import scala.swing.Label

class TruckTicketsAnalyzerOptions extends UserOptions("analyzer.truck.tickets") {
  protected val defaultResult = Map[String, String](
    "output" -> "raw",
    "limit" -> "50",
    "members" -> "example1, example2",
    "members-action" -> "excluded",
    "filter-change-status" -> "false",
    "filter-reporter" -> "false",
    "filter-comment" -> "false",
    "filter-commit" -> "false",
    "filter-comment-commit" -> "false",
    "filter-status" -> "false",
    "min-commits" -> "1",
    "min-commits2" -> "1",
    "min-comments" -> "1",
    "min-comments2" -> "1",
    "min-state-changes" -> "1",
    "algorithm" -> "log")

  protected val htmlDescription = "<b>Truck Number</b><br />How critical is knowledge?"

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Limit output to X lines", text("limit"))
    panel.add("Weight Algorithm", combobox("algorithm", TruckTicketsAnalyzer.weightAlgorithms.keySet.toSeq.sorted))
    panel.add("Members to include/exclude", text("members"))
    panel.add("Tread members list as", combobox("members-action", Seq("included", "excluded")))
    panel.add("Output", combobox("output", Seq("interpreted", "raw")))
    panel.addSpace
    panel.add("A person with knowledge must", new Label(""))
    panel.add("change the status", checkbox("filter-change-status", "At least"),
      text("min-state-changes"),
      label("changes are required"))
    panel.add("not be the reporter", checkbox("filter-reporter", "Ignore the reporter"))
    panel.add("wrote a comment", checkbox("filter-comment", "At least"),
      text("min-comments"),
      label("comments are required"))
    panel.add("wrote a commit", checkbox("filter-commit", "At least"),
      text("min-commits"),
      label("commits are required"))
    panel.add("wrote a comment or commit", checkbox("filter-comment-commit",
      "At least"), text("min-comments2"), label("comments OR"),
      text("min-commits2"), label("commits are required"))
    panel.add("own the ticket in a non-new state", checkbox("filter-status",
      "The ticket must be processed by the person"))
  }
}