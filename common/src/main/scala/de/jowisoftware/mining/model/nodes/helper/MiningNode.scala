package de.jowisoftware.mining.model.nodes.helper

import de.jowisoftware.neo4j.{ DBWithTransaction, ReadOnlyDatabase, ReadWriteDatabase }
import de.jowisoftware.neo4j.content.Node

trait MiningNode extends Node {
  final def id = content.getId
  override final def readableDb = super.readableDb.asInstanceOf[ReadOnlyDatabase]
  override final def writableDb = super.writableDb.asInstanceOf[ReadWriteDatabase]
}