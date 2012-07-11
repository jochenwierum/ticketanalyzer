package de.jowisoftware.mining.model.nodes.helper

import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.Node

trait MiningNode extends Node {
  def id = content.getId
  def rootNode = db.rootNode.asInstanceOf[RootNode]
  override def db = super.db.asInstanceOf[DBWithTransaction[RootNode]]
}