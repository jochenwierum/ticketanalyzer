package de.jowisoftware.mining.model.nodes.helper

import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.ReadOnlyDatabase
import de.jowisoftware.neo4j.ReadWriteDatabase

trait MiningNode extends Node {
  final def id = content.getId
  final def rootNode = readableDb.rootNode
  override final def readableDb = super.readableDb.asInstanceOf[ReadOnlyDatabase[RootNode]]
  override final def writableDb = super.writableDb.asInstanceOf[ReadWriteDatabase[RootNode]]
}