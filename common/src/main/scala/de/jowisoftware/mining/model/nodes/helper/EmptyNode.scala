package de.jowisoftware.mining.model.nodes.helper

trait EmptyNode extends MiningNode {
  val version = 1
  def updateFrom(version: Int) = {}
}