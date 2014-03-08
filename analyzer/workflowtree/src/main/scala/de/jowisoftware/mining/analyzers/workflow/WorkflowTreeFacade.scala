package de.jowisoftware.mining.analyzers.workflow

import de.jowisoftware.mining.analyzer.{Analyzer, AnalyzerResult}
import de.jowisoftware.mining.gui.ProgressMonitor
import de.jowisoftware.neo4j.{DBWithTransaction, Database}

class WorkflowTreeFacade extends Analyzer {
  def userOptions = new WorkflowTreeOptions

  def analyze(db: DBWithTransaction, options: Map[String, String], waitDialog: ProgressMonitor): AnalyzerResult =
    new WorkflowTreeAnalyzer(db, options, waitDialog).run()
}
