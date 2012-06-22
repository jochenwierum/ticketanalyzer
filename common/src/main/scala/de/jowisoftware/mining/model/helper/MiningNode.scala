package de.jowisoftware.mining.model.helper

import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.neo4j.DBWithTransaction

trait MiningNode extends de.jowisoftware.neo4j.content.Node {
  def id = content.getId
  def rootNode = db.rootNode.asInstanceOf[RootNode]
  override def db = super.db.asInstanceOf[DBWithTransaction[RootNode]]
}