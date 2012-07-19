package de.jowisoftware.mining.analyzer

import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.model.nodes.RootNode
import scala.swing.Frame
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.UserOptions

trait Analyzer {
  def analyze(db: Database[RootNode], options: Map[String, String], parent: Frame)
  def userOptions: UserOptions
}