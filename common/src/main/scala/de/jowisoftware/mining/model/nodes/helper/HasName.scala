package de.jowisoftware.mining.model.nodes.helper

protected trait HasName extends MiningNode {
  lazy val name = stringProperty("name")
}