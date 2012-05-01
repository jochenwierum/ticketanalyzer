package de.jowisoftware.mining.linker

sealed abstract class Link

case class ScmLink(
  ref: String,
  isAbbrev: Boolean = false,
  path: Option[String] = None) extends Link

case class TicketLink(id: String) extends Link