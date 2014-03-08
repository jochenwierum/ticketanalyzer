package de.jowisoftware.mining.analyzer.truck.tickets

import de.jowisoftware.mining.analyzer.{ Analyzer, AnalyzerResult }
import de.jowisoftware.mining.gui.ProgressMonitor
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }

class TruckTicketsAnalyzerFacade extends Analyzer {
  def userOptions() = new TruckTicketsAnalyzerOptions()

  def analyze(db: DBWithTransaction, options: Map[String, String],
    waitDialog: ProgressMonitor): AnalyzerResult =
    new TruckTicketsAnalyzer(db, options, waitDialog).run()
}