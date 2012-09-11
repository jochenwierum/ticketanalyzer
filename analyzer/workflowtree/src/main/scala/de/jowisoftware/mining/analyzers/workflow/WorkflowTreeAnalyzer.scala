package de.jowisoftware.mining.analyzers.workflow

import scala.collection.mutable.{ Buffer, StringBuilder }
import scala.swing.Frame
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.{ RootNode, Status, Ticket }
import de.jowisoftware.mining.model.relationships.{ HasStatus, Updates }
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.model.relationships.Owns
import de.jowisoftware.mining.model.nodes.Person
import de.jowisoftware.mining.external.dot.ImageDialog
import java.io.File
import de.jowisoftware.mining.external.dot.DotWrapper
import scala.annotation.tailrec
import scala.collection.SortedMap
import scala.math.Ordering

class WorkflowTreeAnalyzer(db: Database[RootNode], options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) {
  require(options contains "dot")
  private val dotFile = new File(options("dot"))
  require(dotFile.exists())

  require(options contains "dpi")
  require(options("dpi") matches """^\d+$""")
  require(options("dpi").toInt > 0)

  private var workflowTree: SortedMap[List[String], Int] =
    SortedMap.empty[List[String], Int](new Ordering[List[String]]() {
      def compare(l1: List[String], l2: List[String]) =
        if (l1.size < l2.size) -1
        else if (l1.size > l2.size) +1
        else
          l1 zip l2 find (x => x._1 != x._2) match {
            case None => 0
            case Some((x1, x2)) => x1 compare x2
          }
    })

  def run() {
    waitDialog.max = getTickets.size

    getTickets.foreach { t =>
      waitDialog.tick()
      processVersions(t, false)
    }

    val dotWrapper = new DotWrapper(dotFile)
    val image = dotWrapper.run(generateDotCode)

    waitDialog.hide
    val resultDialog = new ImageDialog(image, parent, "Ticket state workflow tree structure")
    resultDialog.visible = true
  }

  private def getTickets =
    for {
      repository <- db.rootNode.ticketRepositoryCollection.children
      ticket <- repository.tickets
      if (ticket.isRootVersion)
    } yield ticket

  private def processVersions(baseTicket: Ticket, includeOwnerChange: Boolean) {
    @tailrec
    def processVersion(ticket: Ticket, oldStatus: List[String], oldOwner: String) {
      val currentStatus = status(ticket)
      val newOwner = owner(ticket)

      val ownerChange = (newOwner != oldOwner)
      val statusChange = (oldStatus == Nil || currentStatus != oldStatus.head)

      val newStatus = if (statusChange || (ownerChange && includeOwnerChange)) {
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

  private def status(ticket: Ticket): String =
    ticket.getFirstNeighbor(Direction.OUTGOING, HasStatus.relationType, Status)
      .map { _.name() } getOrElse "?"

  private def owner(ticket: Ticket): String =
    ticket.getFirstNeighbor(Direction.INCOMING, Owns.relationType, Person)
      .map { _.name() } getOrElse "?"

  private def addStatus(status: List[String]) =
    workflowTree += status -> (workflowTree.getOrElse(status, 0) + 1)

  private def generateDotCode() = {
    def nice(s: String) = s.replaceAll("[^A-Za-z0-9]+", "")
    def listToName(list: List[String]) = list.map(nice).mkString("_")

    val relDepthCount = calcRelativeCount

    val result = new StringBuilder
    result append "digraph {\n"
    result append "dpi=" append options("dpi") append ";\n"
    result append "rankdir=TD;\n"

    for ((nameList, count) <- workflowTree) {
      result append listToName(nameList) append " [label=\"" append
        nameList.head append ": " append count append " (" append
        (100.0 * relDepthCount(nameList)).formatted("%.2f %%") append ")\"];\n"
      if (nameList.tail != Nil) {
        val factor = (100.0 * count / workflowTree(nameList.tail)).formatted("%.2f %%")
        result append listToName(nameList.tail) append " -> " append
          listToName(nameList) append " [label =\"" append factor append
          "\"];\n"
      }
    }

    result append "}\n"

    result.toString
  }

  private def calcRelativeCount = {
    var relDepthCount = Map[List[String], Double](Nil -> 1.0)

    val levelCount = (Map[Int, Int]() /: workflowTree) {
      case (map, (name, count)) =>
        val l = name.length
        map + (l -> (map.getOrElse(l, 0) + count))
    }

    for ((key, value) <- workflowTree) {
      relDepthCount += key -> ((value.doubleValue / levelCount(key.length))
        * relDepthCount(key.tail))
    }

    relDepthCount
  }
}
