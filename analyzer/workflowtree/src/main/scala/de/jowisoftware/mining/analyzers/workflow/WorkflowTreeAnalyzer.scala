package de.jowisoftware.mining.analyzers.workflow

import java.io.File

import scala.annotation.tailrec
import scala.collection.SortedMap
import scala.collection.mutable
import scala.swing.Frame

import org.neo4j.graphdb.Direction

import de.jowisoftware.mining.external.dot.{ DotWrapper, ImageDialog }
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.{ Person, RootNode, Status, Ticket }
import de.jowisoftware.mining.model.relationships.{ HasStatus, Owns, Updates }
import de.jowisoftware.neo4j.Database

object WorkflowTreeAnalyzer {
  private case class Node(name: String, label: String, count: Int, factor: Double,
      var finalCount: Int, var ignoredCount: Int = 0, var relations: List[Relation] = Nil) {
    def toStringBuilder(builder: mutable.StringBuilder) =
      builder append name append
        """ [label="""" append label append """\ntotal: """ append
        count append " (" append perCent(factor) append ")" append
        """\nfinal: """ append perCentWithLabel(finalCount, count) append
        """\nignored: """ append perCentWithLabel(ignoredCount, count) append
        "\"];\n"
  }

  private case class Relation(to: Node, factor: Double) {
    def toStringBuilder(builder: mutable.StringBuilder, from: String) =
      builder append from append " -> " append to.name append " [label = \"" append
        perCent(factor)+"\"];\n"
  }

  private def status(ticket: Ticket): String =
    ticket.getFirstNeighbor(Direction.OUTGOING, HasStatus.relationType, Status)
      .map { _.name() } getOrElse "?"

  private def owner(ticket: Ticket): String =
    ticket.getFirstNeighbor(Direction.INCOMING, Owns.relationType, Person)
      .map { _.name() } getOrElse "?"

  private def listToName(list: List[String]) = list.map {
    _.replaceAll("[^A-Za-z0-9]+", "")
  }.mkString("_")

  private def perCentWithLabel(a: Int, b: Int) = a+" ("+perCent(a.floatValue / b)+")"
  private def perCent(f: Double) = (100.0 * f).formatted("%.2f %%")
}

class WorkflowTreeAnalyzer(db: Database[RootNode], options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) {
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
      processVersions(t, options("ownerChange").toLowerCase() == "true")
    }

    val dotWrapper = new DotWrapper(dotFile)
    val image = dotWrapper.run(
      generateDotCode(options("edgeThreshold").toFloat,
        options("nodeThreshold").toFloat))

    waitDialog.hide
    val resultDialog = new ImageDialog(image, parent,
      "Ticket state workflow tree structure")
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
      val statusChange = (oldStatus.isEmpty || currentStatus != oldStatus.head)

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

  private def addStatus(status: List[String]) =
    workflowTree += status -> (workflowTree.getOrElse(status, 0) + 1)

  private def generateDotCode(nodeThreshold: Float, edgeThreshold: Float) = {
    val relDepthCount = calcRelativeCount

    val result = new mutable.StringBuilder
    result append "digraph {\n"
    result append "dpi=" append options("dpi") append ";\n"
    result append "rankdir=TD;\n"

    createDotBody(relDepthCount, result)

    result append "}\n"
    result.toString
  }

  private def createDotBody(
    relDepthCount: Map[List[String], Double], result: StringBuilder) {

    val rootNode = makeTree(relDepthCount)
    convertTreeToDot(rootNode, result)
  }

  private def makeTree(relDepthCount: Map[List[String], Double]) = {
    val rootNode = Node("root", "", 0, 0f, 0)

    def findParent(parentNames: List[String]): Option[Node] = parentNames match {
      case Nil => Some(rootNode)
      case head :: tail =>
        findParent(tail).flatMap { parent => parent.relations.map(_.to).find(_.label == head) }
    }

    for ((nameList, count) <- workflowTree) {
      val nodeFactor = relDepthCount(nameList)
      val edgeFactor = calcEdgeFactor(count, nameList)

      findParent(nameList.tail) match {
        case None =>
        case Some(parentNode) =>
          if (nodeFactor >= nodeThreshold && edgeFactor >= edgeThreshold) {
            val node = Node(listToName(nameList), nameList.head, count, nodeFactor, count)
            val relation = Relation(node, edgeFactor)
            parentNode.relations = relation :: parentNode.relations
          } else {
            parentNode.ignoredCount += count
          }
          parentNode.finalCount -= count
      }
    }

    rootNode
  }

  private def convertTreeToDot(rootNode: Node, result: StringBuilder) {
    def dumpNode(node: Node) {
      node.toStringBuilder(result)

      node.relations.foreach { relation =>
        dumpNode(relation.to)
        relation.toStringBuilder(result, node.name)
      }
    }

    for (realRootRelation <- rootNode.relations) {
      dumpNode(realRootRelation.to)
    }
  }

  private def calcEdgeFactor(count: Int, nameList: List[String]) =
    if (nameList.tail.isEmpty) 1.0
    else (count.floatValue / workflowTree(nameList.tail))

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
