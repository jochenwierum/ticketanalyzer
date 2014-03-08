package de.jowisoftware.mining.analyzer.workflow

import scala.collection.immutable.Map

import de.jowisoftware.mining.analyzer.{Analyzer, AnalyzerResult}
import de.jowisoftware.mining.gui.ProgressMonitor
import de.jowisoftware.neo4j.DBWithTransaction

class WorkflowAnalyzerFacade extends Analyzer {
  def analyze(db: DBWithTransaction, options: Map[String, String], waitDialog: ProgressMonitor): AnalyzerResult =
    new WorkflowAnalyzer(db, options, waitDialog).run()

  def userOptions() = new WorkflowUserOptions
}