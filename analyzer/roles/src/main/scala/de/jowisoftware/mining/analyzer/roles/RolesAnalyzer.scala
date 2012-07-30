package de.jowisoftware.mining.analyzer.roles

import de.jowisoftware.mining.gui.ProgressDialog
import scala.collection.immutable.Map
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.analyzer.Analyzer
import scala.swing.Frame
import de.jowisoftware.mining.model.nodes.RootNode

class RolesAnalyzer extends Analyzer {
  def userOptions() = new RolesOptions()

  def analyze(db: Database[RootNode], options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) = {

  }
}