package de.jowisoftware.mining.analyzers.workflow

import scala.swing.Frame

import de.jowisoftware.mining.analyzer.Analyzer
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database

class WorkflowTreeFacade extends Analyzer {
  def userOptions = new WorkflowTreeOptions

  def analyze(db: Database, options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) =
    new WorkflowTreeAnalyzer(db, options, parent, waitDialog).run()
}
