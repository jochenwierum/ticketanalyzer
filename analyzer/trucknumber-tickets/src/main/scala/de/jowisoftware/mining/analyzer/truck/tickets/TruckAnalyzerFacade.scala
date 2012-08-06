package de.jowisoftware.mining.analyzer.truck.tickets

import scala.swing.Frame

import de.jowisoftware.mining.analyzer.Analyzer
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database

class TruckAnalyzerFacade extends Analyzer {
  def userOptions() = new TruckAnalyzerOptions()

  def analyze(db: Database[RootNode], options: Map[String, String],
    parent: Frame, waitDialog: ProgressDialog) =
    new TruckAnalyzer(db, options, parent, waitDialog).run()

}