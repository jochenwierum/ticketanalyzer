package de.jowisoftware.mining.linker

import de.jowisoftware.mining.model.Node

trait LinkEvents {
  def reportProgress(progress: Long, max: Long, message: String)
  def finish()

  def foundLink(source: Node, link: Link)
}