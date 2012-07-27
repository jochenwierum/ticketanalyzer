package de.jowisoftware.mining.analyzer.structure

import de.jowisoftware.mining.gui.ProgressDialog
import scala.collection.immutable.Map
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.analyzer.Analyzer
import scala.swing.Frame
import de.jowisoftware.mining.model.nodes.RootNode

class StructureAnalyzerFacade extends Analyzer {
  def analyze(db: Database[RootNode], options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) =
    new StructureAnalyzer(db, options, parent, waitDialog).run()

  def userOptions() = new StructreUserOptions
}