package de.jowisoftware.mining.analyzer.workflow

import java.io.File
import java.util.Locale

import scala.collection.immutable.Map
import scala.swing.{ Dialog, Frame, Swing }

import org.neo4j.cypher.ExecutionResult

import de.jowisoftware.mining.analyzer.data.TextMatrix
import de.jowisoftware.mining.external.dot.{ DotWrapper, ImageDialog }
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.TicketRepository
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }

class WorkflowAnalyzer(db: Database,
    options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) {

  require(options contains "visualization")
  require(options contains "dpi")
  require(options("dpi") matches """^\d+$""")
  require(options("dpi").toInt > 0)

  if (options("visualization") == "Graph")
    require(new File(options("dot")).exists)

  def run(): Unit = {
    val resultWindow: Dialog = db.inTransaction { transaction =>
      val result = findStateChanges(transaction)
      val deadEnds = createDeadEndMap(findDeadEnds(transaction))

      options("visualization") match {
        case "Graph" => createDotWindow(result, deadEnds, parent)
        case "Matrix" => createMatrixWindow(result, deadEnds, parent)
      }
    }

    Swing.onEDTWait {
      waitDialog.hide()
      resultWindow.visible = true
    }
  }

  private def findStateChanges(transaction: DBWithTransaction): ExecutionResult =
    transaction.cypher(s"""
      MATCH ${TicketRepository.cypherForAll("node")} --> ticket1 -[:has_status]-> status1,
        status2 <-[:has_status]- ticket2 -[:updates]-> ticket1,
        ticket1 <-[:owns]- owner1, ticket2 <-[:owns]- owner2
      WHERE status1 <> status2 OR owner1 <> owner2
      RETURN status1.name AS from, status2.name AS to, count(*) AS count
      ORDER BY from, count DESC;
      """)

  private def findDeadEnds(transaction: DBWithTransaction): ExecutionResult =
    transaction.cypher(s"""
      MATCH
        ${TicketRepository.cypherForAll("node")} --> ticket -[:has_status]-> status
      WHERE NOT (ticket) <-[:updates]- ()
      RETURN status.name AS name, count(*) AS count;
    """)

  private def createDeadEndMap(result: ExecutionResult) = {
    val mapIterator = for (row <- result) yield {
      row("name").asInstanceOf[String] -> row("count").asInstanceOf[Long]
    }

    mapIterator.toMap
  }

  private def createMatrixWindow(result: ExecutionResult, deadEnds: Map[String, Long], parent: Frame): Dialog = {
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
      matrix.set("(final)", status._1, status._2)
    }

    for (row <- buffered) {
      matrix.set(row("to").asInstanceOf[String], row("from").asInstanceOf[String], row("count").asInstanceOf[Long])
    }

    new MatrixDialog(matrix, parent)
  }

  private def createDotWindow(result: ExecutionResult, deadEnds: Map[String, Long], parent: Frame): Dialog = {
    val (lines, nodeNames) = formatResultToDotNodes(result)
    val graphText = "digraph {"+
      "concentrate=true;\n"+
      "dpi="+options("dpi")+";\n"+
      getNodesStrings(nodeNames, deadEnds).mkString("\n\t", "\n\t", "\n") +
      lines.mkString("\n\t", "\n\t", "\n")+
      "}"

    val graph = new DotWrapper(new File(options("dot"))).run(graphText)

    new ImageDialog(graph, parent, "Ticket state workflow structure")
  }

  private def getNodesStrings(names: Map[String, String], deadEnds: Map[String, Long]): Iterable[String] =
    (names.map {
      case (id, text) =>
        """%s [label = "%s (final: %d)"];""" format (id, text, deadEnds.getOrElse(text, 0))
    }) ++ ((deadEnds.keySet -- names.keySet) map {
      case text =>
        """%s [label = "%s (final: %d)"];""" format (nodeName(text), text, deadEnds(text))
    })

  private def formatResultToDotNodes(result: ExecutionResult): (List[String], Map[String, String]) = {
    var nodeNames: Map[String, String] = Map()
    var lines: List[String] = Nil

    val resultList = (for (row <- result) yield {
      (row("from").asInstanceOf[String],
        row("to").asInstanceOf[String], row("count").asInstanceOf[Long])
    }).toList.groupBy(_._1)

    for ((from, targetsWithCount) <- resultList) {
      val max = targetsWithCount.maxBy(_._3)._3
      val sum = targetsWithCount.foldLeft(0L)(_ + _._3)

      val fromNodeName = nodeName(from)
      nodeNames += fromNodeName -> from

      for ((_, to, count) <- targetsWithCount) {
        val toNodeName = nodeName(to)
        nodeNames += toNodeName -> to

        val countDouble = count.doubleValue
        lines = getEdgeString(fromNodeName, toNodeName,
          count, countDouble / max, countDouble / sum) :: lines
      }
    }

    (lines, nodeNames)
  }

  private def nodeName(s: String) = s
    .replaceAll("[^A-Za-z0-9]+", "_")
    .replaceAll("^_+|_+$", "")

  def getEdgeString(from: String, to: String, count: Long, colorFactor: Double, textFactor: Double) = {
    val weight = (colorFactor * 1000).intValue
    val width = (3 * colorFactor) max 1
    val red = (colorFactor * 255).intValue

    """%s -> %s [weight = %d, label = "%s (%s %%)", penwidth = %f, """+
      """color = "#%2x0000", fontcolor="#%7$2x0000"];""" formatLocal (
        Locale.ENGLISH,
        from, to, weight, count, (textFactor * 100).formatted("%.2f"), width, red)
  }
}