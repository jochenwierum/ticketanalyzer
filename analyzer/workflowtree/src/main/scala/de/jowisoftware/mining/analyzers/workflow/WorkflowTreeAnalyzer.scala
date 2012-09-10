package de.jowisoftware.mining.analyzers.workflow

import scala.collection.mutable.{Buffer, StringBuilder}
import scala.swing.Frame

import org.neo4j.graphdb.Direction

import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.{RootNode, Status, Ticket}
import de.jowisoftware.mining.model.relationships.{HasStatus, Updates}
import de.jowisoftware.neo4j.Database

class WorkflowTreeAnalyzer(db: Database[RootNode], options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) {
  private val workflowTree: Buffer[Map[(String, String), Int]] = Buffer()

  def run() {
    waitDialog.max = getTickets.size

    getTickets.foreach { t =>
      waitDialog.tick()
      processVersions(t)
    }

    val code = generateDotCode

    waitDialog.hide

    println(workflowTree)
    println(code)
  }

  private def getTickets =
    for {
      repository <- db.rootNode.ticketRepositoryCollection.children
      ticket <- repository.tickets
      if (ticket.isRootVersion)
    } yield ticket

  private def processVersions(baseTicket: Ticket) {
    // TODO: add choice whether to to ignore owner changes
    // FIXME: only track status changes
    def processVersion(ticket: Ticket, oldStatus: String, depth: Int) {
      val newStatus = status(ticket)
      addStatus(depth, oldStatus, newStatus)

      ticket.getFirstNeighbor(Direction.INCOMING, Updates.relationType, Ticket) match {
        case Some(newTicket) =>
          processVersion(newTicket, newStatus, depth + 1)
        case None =>
      }
    }

    processVersion(baseTicket, "", 0)
  }

  private def status(ticket: Ticket): String =
    ticket.getFirstNeighbor(Direction.OUTGOING, HasStatus.relationType, Status)
      .map {_.name()} getOrElse "?"

  private def addStatus(depth: Int, oldStatus: String, status: String) {
    if (workflowTree.size < depth + 1)
      workflowTree += Map()

    val oldValue = workflowTree(depth).getOrElse((oldStatus, status), 0)
    workflowTree(depth) += (oldStatus, status) -> (oldValue + 1)
  }

  private def generateDotCode() = {
    val result = new StringBuilder

    def nice(s: String) = s.replaceAll("[^A-Za-z0-9]+", "")

    result append "digraph {\n"
    result append "rankdir=TD;\n"

    for ((map, level) <- workflowTree.zipWithIndex) {
      result append ("subgraph cluster_" + level + "{\n")
      result append map.keys.map(_._2).toSet.map { name: String =>
          "n"+level+nice(name)+" [label=\""+name+"\"];"
        }.mkString("\n")
      result append "}\n"
    }

    for {
        (map, level) <- workflowTree.zipWithIndex
        if level > 0
      } {
        for (((from, to), count) <- map) {
          result append "n"+(level-1)+nice(from)+" -> n"+(level)+nice(to)+";\n"
        }
    }

    result append "}\n"

    result.toString
  }
}
