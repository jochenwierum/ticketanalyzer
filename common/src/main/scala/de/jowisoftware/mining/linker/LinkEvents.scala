package de.jowisoftware.mining.linker

import de.jowisoftware.mining.model.nodes.helper.MiningNode

trait LinkEvents {
  def reportProgress(progress: Long, max: Long, message: String)
  def finish()

  def foundLink(source: MiningNode, link: Link)
}