package de.jowisoftware.mining.model.nodes.helper

object HasName {
  object properties {
    val name = "name"
  }
}

trait HasName extends MiningNode {
  lazy val name = stringProperty(HasName.properties.name)
}
