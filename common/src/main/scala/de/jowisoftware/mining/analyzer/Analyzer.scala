package de.jowisoftware.mining.analyzer

import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.gui.ProgressMonitor
import de.jowisoftware.neo4j.DBWithTransaction

trait Analyzer {
  def analyze(db: DBWithTransaction, options: Map[String, String],
    waitDialog: ProgressMonitor): AnalyzerResult
  def userOptions: UserOptions
}