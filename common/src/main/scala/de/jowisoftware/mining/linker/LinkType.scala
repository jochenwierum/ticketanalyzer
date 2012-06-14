package de.jowisoftware.mining.linker

object LinkType extends Enumeration {
  type LinkType = Value

  val Mentions, DependsOn, Blocks = Value
}