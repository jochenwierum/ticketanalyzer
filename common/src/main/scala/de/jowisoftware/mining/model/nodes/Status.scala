package de.jowisoftware.mining.model.nodes

import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import helper._

object Status extends IndexedNodeCompanion[Status] {
  def apply = new Status
  val indexInfo = IndexedNodeInfo("status")
}

class Status extends MiningNode with HasName {
  val version = 1
  def updateFrom(version: Int) {}

  lazy val logicalType = optionalIntProperty("logicalType")

}