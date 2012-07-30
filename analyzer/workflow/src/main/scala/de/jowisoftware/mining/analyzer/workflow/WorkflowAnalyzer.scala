package de.jowisoftware.mining.analyzer.workflow

import de.jowisoftware.mining.gui.ProgressDialog
import scala.collection.immutable.Map
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.analyzer.Analyzer
import scala.swing.Frame
import de.jowisoftware.mining.model.nodes.RootNode
import org.neo4j.graphdb.Direction
import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.mining.model.nodes.CommitRepository
import org.neo4j.cypher.ExecutionEngine
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import de.jowisoftware.mining.model.nodes.TicketRepository
import java.util.Locale
import java.io.File
import org.neo4j.cypher.ExecutionResult
import scala.swing.Dialog
import de.jowisoftware.mining.analyzer.data.TextMatrix

class WorkflowAnalyzer(db: Database[RootNode],
    options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) {

  lazy val repositoryNodes = db.rootNode.ticketRepositoryCollection.children
    .map(_.id).mkString(", ")

  def run() {
    val result = findStateChanges
    val deadEnds = createDeadEndMap(findDeadEnds)

    val resultWindow: Dialog = options("visualization") match {
      case "Graph" => createDotWindow(result, deadEnds)
      case "Matrix" => createMatrixWindow(result, deadEnds)
    }

    waitDialog.hide
    resultWindow.visible = true
  }

  private def findStateChanges: ExecutionResult = {
    val query = """
      START repository=node(%s) // ticket collection
      MATCH repository --> ticket1 -[:has_status]-> status1,
        status2 <-[:has_status]- ticket2 -[:updates]-> ticket1
      WHERE status1 <> status2
      RETURN status1.name AS from, status2.name AS to, count(*) AS count
      ORDER BY from, count DESC;
      """ format (repositoryNodes)

    val engine = new ExecutionEngine(db.service)
    val result = engine.execute(query)
    result
  }

  private def findDeadEnds: ExecutionResult = {
    val query = """
      START repository=node(%s) // ticket collection
      MATCH
        repository --> ticket1 -[:has_status]-> status,
        ticket2 -[r?:updates]-> ticket1
      WHERE ticket2 IS NULL
      RETURN status.name AS name, count(*) AS count;
    """ format (repositoryNodes)

    val engine = new ExecutionEngine(db.service)
    val result = engine.execute(query)
    result
  }

  private def createDeadEndMap(result: ExecutionResult) = {
    val mapIterator = for (row <- result) yield {
      row("name").asInstanceOf[String] -> row("count").asInstanceOf[Long]
    }

    mapIterator.toMap
  }

  private def createMatrixWindow(result: ExecutionResult, deadEnds: Map[String, Long]): Dialog = {
    val buffered = result.toSeq
    var namesSet: Set[String] = Set()

    for (row <- buffered) {
      namesSet += row("from").asInstanceOf[String]
      namesSet += row("to").asInstanceOf[String]
    }

    for (dead <- deadEnds.keys) {
      namesSet += dead
    }

    val titles = namesSet.toSeq.sorted
    val matrix = new TextMatrix(titles :+ "(final)", titles)

    for (status <- deadEnds) {
      matrix.set("(final)", status._1 , status._2)
    }

    for (row <- buffered) {
      matrix.set(row("to").asInstanceOf[String], row("from").asInstanceOf[String], row("count").asInstanceOf[Long])
    }

    new MatrixDialog(matrix)
  }

  private def createDotWindow(result: ExecutionResult, deadEnds: Map[String, Long]): Dialog = {
    val (lines, nodeNames) = formatResultToDotNodes(result)
    val graphText = "digraph {"+
      getNodesStrings(nodeNames, deadEnds).mkString("\n\t", "\n\t", "\n") +
      lines.mkString("\n\t", "\n\t", "\n")+
      "}"

    val graph = new DotWrapper(new File(options("dot"))).run(graphText)

    new ImageDialog(graph)
  }

  def getEdgeString(from: String, to: String, count: Long, factor: Double) = {
    val weight = ((1 - factor) * 200).intValue
    val width = (3 * factor) max 1
    val red = (factor * 255).intValue

    """%s -> %s [weight = %d, label = "%s", penwidth = %f, color = "#%2x0000", fontcolor="#%6$2x0000"];""" formatLocal (
      Locale.ENGLISH,
      from, to, weight, count, width, red)
  }

  private def formatResultToDotNodes(result: ExecutionResult): (List[String], Map[String, String]) = {
    var lastFrom = ""
    var lastCount = 0L
    var nodeNames: Map[String, String] = Map()
    var lines: List[String] = Nil

    for (row <- result) {
      val from = row("from").asInstanceOf[String]
      val to = row("to").asInstanceOf[String]
      val count = row("count").asInstanceOf[Long]

      if (from != lastFrom) {
        lastFrom = from
        lastCount = count
      }

      val fromNodeName = nodeName(from)
      val toNodeName = nodeName(to)

      nodeNames += fromNodeName -> from
      nodeNames += toNodeName -> to
      lines = getEdgeString(fromNodeName, toNodeName, count, count.doubleValue / lastCount) :: lines
    }

    (lines, nodeNames)
  }

  private def nodeName(s: String) = s
    .replaceAll("[^A-Za-z0-9]+", "_")
    .replaceAll("^_+|_+$", "")

  private def getNodesStrings(names: Map[String, String], deadEnds: Map[String, Long]): Iterable[String] =
    names.map {
      case (id, text) =>
        """%s [label = "%s; final: %d"];""" format (id, text, deadEnds(id))
    }
}