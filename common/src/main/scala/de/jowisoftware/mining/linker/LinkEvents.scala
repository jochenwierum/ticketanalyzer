package de.jowisoftware.mining.linker

import de.jowisoftware.mining.model.nodes.helper.MiningNode
import de.jowisoftware.mining.model.nodes.Status

trait LinkEvents {
  def reportProgress(progress: Long, max: Long, message: String)
  def finish()

  def foundLink(source: MiningNode, link: Link)
  def foundStatusMap(status: Status, statusType: StatusType.Value)
  def foundKeywords(source: MiningNode, keywords: Set[String])
}