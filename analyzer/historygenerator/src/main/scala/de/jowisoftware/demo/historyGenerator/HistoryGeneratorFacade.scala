package de.jowisoftware.demo.historyGenerator

import de.jowisoftware.mining.analyzer.{Analyzer, AnalyzerResult}
import de.jowisoftware.mining.gui.ProgressMonitor
import de.jowisoftware.neo4j.{DBWithTransaction, Database}

class HistoryGeneratorFacade extends Analyzer {
  def userOptions = new HistoryGeneratorOptions

  def analyze(db: DBWithTransaction, options: Map[String, String], waitDialog: ProgressMonitor): AnalyzerResult =
    new HistoryGeneratorAnalyzer(db, options, waitDialog).run()
}
