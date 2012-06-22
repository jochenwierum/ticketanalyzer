package de.jowisoftware.mining.model.helper

protected trait HasName extends MiningNode {
  lazy val name = stringProperty("name")
}
