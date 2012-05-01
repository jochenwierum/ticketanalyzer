package de.jowisoftware.mining.linker

import de.jowisoftware.mining.model.{ Ticket, Node }
import de.jowisoftware.mining.model.Commit

class DatabaseLinkerHandler extends LinkEvents {
  def reportProgress(progress: Long, max: Long, message: String): Unit = {}
  def finish(): Unit = {}

  def foundLink(source: Node, link: Link) {
    println("Link from "+(source match {
      case t: Ticket => t.id()
      case c: Commit => c.id()
      case _ => "?"
    })+" to "+link)
  }
}