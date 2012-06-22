package de.jowisoftware.mining.model.helper

protected trait EmptyNode extends MiningNode {
  val version = 1
  def updateFrom(version: Int) = {}
}