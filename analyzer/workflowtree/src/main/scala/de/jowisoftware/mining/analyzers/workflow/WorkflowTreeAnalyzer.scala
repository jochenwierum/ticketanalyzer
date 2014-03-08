package de.jowisoftware.mining.analyzers.workflow

import java.io.File
import scala.annotation.tailrec
import scala.collection.mutable
import org.neo4j.graphdb.{ Direction, Node => NeoNode }
import de.jowisoftware.mining.analyzer.ImageResult
import de.jowisoftware.mining.external.dot.{ DotWrapper, ImageDialog }
import de.jowisoftware.mining.gui.ProgressMonitor
import de.jowisoftware.mining.model.nodes.{ Person, Status, Ticket, TicketRepository }
import de.jowisoftware.mining.model.relationships.{ HasStatus, Owns, Updates }
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }
import de.jowisoftware.neo4j.content.{ Node => CNode }
import de.jowisoftware.mining.analyzer.AnalyzerResult

object WorkflowTreeAnalyzer {
  private def status(ticket: Ticket): String =
    ticket.getFirstNeighbor(Direction.OUTGOING, HasStatus.relationType, Status)
      .map { _.name() } getOrElse "?"

  private def owner(ticket: Ticket): String =
    ticket.getFirstNeighbor(Direction.INCOMING, Owns.relationType, Person)
      .map { _.name() } getOrElse "?"

  private def listToName(list: List[String]) = list.map {
    _.replaceAll("[^A-Za-z0-9]+", "")
  }.mkString("_")

}

class WorkflowTreeAnalyzer(transaction: DBWithTransaction, options: Map[String, String], waitDialog: ProgressMonitor) {
  import WorkflowTreeAnalyzer._

  require(options contains "dot")
  private val dotFile = new File(options("dot"))
  require(dotFile.exists())

  require(options contains "dpi")
  require(options("dpi") matches """^\d+$""")
  require(options("dpi").toInt > 0)

  require(options("nodeThreshold") matches """^\d*(\.\d+)?""")
  require(options("edgeThreshold") matches """^\d*(\.\d+)?""")

  private val nodeThreshold = options("nodeThreshold").toFloat / 100
  private val edgeThreshold = options("edgeThreshold").toFloat / 100
  private val trackOwnerChange = options("ownerChange").toLowerCase() == "true"

  private val highlight = options("highlight").toLowerCase == "true"

  private val rootNode = new Node("root", "")

  private def findNode(parentNames: List[String]): Node = parentNames match {
    case Nil => rootNode
    case head :: tail =>
      findNode(tail).children.map(_.to).find(_.label == head).get
  }

  def run(): AnalyzerResult = {
    val tickets = getTickets
    waitDialog.max = tickets.size

    tickets.foreach { t =>
      waitDialog.tick()
      processVersions(t)
    }

    updateStats

    val dotWrapper = new DotWrapper(dotFile)
    val image = dotWrapper.run(generateDotCode)

    new ImageResult(image, "Ticket state workflow tree structure")
  }

  private def getTickets: Seq[Ticket] =
    transaction.cypher(s"""
          MATCH ${TicketRepository.cypherForAll("n")} -[:contains]-> ticket
          WHERE NOT (ticket) -[:updates]-> ()
          RETURN ticket""").map(ticketMap =>
      CNode.wrapNeoNode(ticketMap("ticket").asInstanceOf[NeoNode], transaction, Ticket)).toSeq

  private def processVersions(baseTicket: Ticket): Unit = {
    @tailrec
    def processVersion(ticket: Ticket, oldStatus: List[String], oldOwner: String) {
      val currentStatus = status(ticket)
      val newOwner = owner(ticket)

      val ownerChange = (newOwner != oldOwner)
      val statusChange = (oldStatus.isEmpty || currentStatus != oldStatus.head)

      val newStatus = if (statusChange || (ownerChange && trackOwnerChange)) {
        val statusList = currentStatus :: oldStatus
        addStatus(statusList)
        statusList
      } else {
        oldStatus
      }

      ticket.getFirstNeighbor(Direction.INCOMING, Updates.relationType, Ticket) match {
        case Some(newTicket) =>
          processVersion(newTicket, newStatus, newOwner)
        case None =>
      }
    }

    processVersion(baseTicket, Nil, "")
  }

  private def addStatus(status: List[String]) = {
    val parent = findNode(status.tail)
    parent.children.find(_.to.label == status.head) map (_.to) match {
      case Some(node) =>
        node.incrementCounts()
      case None =>
        val node = new Node(listToName(status), status.head)
        node.incrementCounts()
        parent.addChild(node)
    }
  }

  private def updateStats {
    rootNode.setCounts((0 /: rootNode.children)(_ + _.to.count))
    rootNode.factor = 1f

    def updateSubtree(node: Node) {
      for (relation <- node.children; child = relation.to) {
        val percent = child.count.doubleValue / node.count

        if (percent > edgeThreshold && percent * node.factor > nodeThreshold) {
          relation.factor = percent
          child.factor = percent * node.factor
          node.addNonFinal(child.count)

          updateSubtree(child)
        } else {
          node.addNonFinal(child.count)
          node.addIgnored(child.count)
          child.hide()
        }
      }
    }

    updateSubtree(rootNode)
  }

  private def generateDotCode = {
    val result = new mutable.StringBuilder
    result append "digraph {\n"
    result append "dpi=" append options("dpi") append ";\n"
    result append "rankdir=TD;\n"

    convertTreeToDot(result)

    result append "}\n"

    result.toString
  }

  private def convertTreeToDot(result: StringBuilder) {
    val highlightedRootRelation = rootNode.children.reduce((r1, r2) =>
      if (r1.to.count > r2.to.count) r1 else r2)

    for (realRootRelation <- rootNode.children) {
      val highlightNode = highlight && realRootRelation == highlightedRootRelation
      realRootRelation.to.toStringBuilder(result, highlightNode)
    }
  }
}
