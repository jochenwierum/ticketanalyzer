package de.jowisoftware.mining.analyzer.truck.tickets

import scala.swing.Frame

import de.jowisoftware.mining.analyzer.Analyzer
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database

class TruckTicketsAnalyzerFacade extends Analyzer {
  def userOptions() = new TruckTicketsAnalyzerOptions()

  def analyze(db: Database, options: Map[String, String],
    parent: Frame, waitDialog: ProgressDialog) =
    new TruckTicketsAnalyzer(db, options, parent, waitDialog).run()
}