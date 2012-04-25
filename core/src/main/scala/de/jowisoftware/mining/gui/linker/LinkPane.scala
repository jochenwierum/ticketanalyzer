package de.jowisoftware.mining.gui.linker

import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.Label
import de.jowisoftware.mining.plugins.PluginManager
import de.jowisoftware.neo4j.Database
import scala.swing.Frame
import de.jowisoftware.mining.model.RootNode

class LinkPane(db: Database[RootNode], pluginManager: PluginManager, parent: Frame) extends BoxPanel(Orientation.Vertical) {
  contents += new Label("Hello World")
}