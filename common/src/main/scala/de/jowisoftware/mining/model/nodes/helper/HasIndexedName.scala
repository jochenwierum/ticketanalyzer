package de.jowisoftware.mining.model.nodes.helper

trait HasIndexedName extends HasName {
  override lazy val name = stringProperty("name", "", true)
}