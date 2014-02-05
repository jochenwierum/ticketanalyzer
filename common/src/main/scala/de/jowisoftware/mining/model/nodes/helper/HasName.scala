package de.jowisoftware.mining.model.nodes.helper

trait HasName extends MiningNode {
  lazy val name = stringProperty("name")
}
