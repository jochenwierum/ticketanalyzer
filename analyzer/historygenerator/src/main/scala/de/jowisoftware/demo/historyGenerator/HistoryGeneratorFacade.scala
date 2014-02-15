package de.jowisoftware.demo.historyGenerator

import scala.swing.Frame

import de.jowisoftware.mining.analyzer.Analyzer
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database

class HistoryGeneratorFacade extends Analyzer {
  def userOptions = new HistoryGeneratorOptions

  def analyze(db: Database, options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) =
    new HistoryGeneratorAnalyzer(db, options, parent, waitDialog).run()
}
