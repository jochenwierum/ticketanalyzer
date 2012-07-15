package de.jowisoftware.mining.linker

sealed abstract class Link

case class ScmLink(
  ref: String,
  linkType: LinkType.Value = LinkType.Mentions,
  path: Option[String] = None) extends Link

case class TicketLink(
  id: Int,
  linkType: LinkType.Value = LinkType.Mentions) extends Link