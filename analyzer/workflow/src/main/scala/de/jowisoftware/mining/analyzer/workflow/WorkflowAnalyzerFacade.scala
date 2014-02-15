package de.jowisoftware.mining.analyzer.workflow

import de.jowisoftware.mining.gui.ProgressDialog
import scala.collection.immutable.Map
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.analyzer.Analyzer
import scala.swing.Frame
import de.jowisoftware.mining.model.nodes.RootNode

class WorkflowAnalyzerFacade extends Analyzer {
  def analyze(db: Database, options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) =
    new WorkflowAnalyzer(db, options, parent, waitDialog).run()

  def userOptions() = new WorkflowUserOptions
}