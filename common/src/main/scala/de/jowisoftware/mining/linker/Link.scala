package de.jowisoftware.mining.linker

sealed abstract class Link

case class ScmLink(
  ref: String,
  linkType: LinkType.LinkType = LinkType.Mentions,
  isAbbrev: Boolean = false,
  path: Option[String] = None) extends Link

case class TicketLink(
  id: String,
  linkType: LinkType.LinkType = LinkType.Mentions) extends Link