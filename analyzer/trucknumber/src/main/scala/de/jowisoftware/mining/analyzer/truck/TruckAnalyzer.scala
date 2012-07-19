package de.jowisoftware.mining.analyzer.truck

import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.analyzer.Analyzer
import scala.swing.Frame
import de.jowisoftware.mining.model.nodes.RootNode

class TruckAnalyzer extends Analyzer {
  def analyze(db: Database[RootNode], options: Map[String, String], parent: Frame): Unit = {}
  def userOptions() = new TruckAnalyzerOptions
}