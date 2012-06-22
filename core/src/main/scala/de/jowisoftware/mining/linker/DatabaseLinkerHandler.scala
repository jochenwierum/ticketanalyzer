package de.jowisoftware.mining.linker

import de.jowisoftware.mining.model.Ticket
import de.jowisoftware.mining.model.helper.MiningNode
import de.jowisoftware.mining.model.Commit

class DatabaseLinkerHandler extends LinkEvents {
  def reportProgress(progress: Long, max: Long, message: String): Unit = {}
  def finish(): Unit = {}

  def foundLink(source: MiningNode, link: Link) {
    println("Link from "+(source match {
      case t: Ticket => t.ticketId()
      case c: Commit => c.commitId()
      case _ => "?"
    })+" to "+link)
  }
}