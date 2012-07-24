package de.jowisoftware.mining.analyzer.structure

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

class StructureAnalyzer extends Analyzer {
  def userOptions() = new StructreUserOptions

  def analyze(db: Database[RootNode],
    options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) = {

    val nodes = db.rootNode.ticketRepositoryCollection.neighbors(
      Direction.OUTGOING, Seq(Contains.relationType))
      .map(_.asInstanceOf[TicketRepository].id)
      .mkString(", ")

    val query = """
      START r=node(%s) // ticket collection
      MATCH r --> n -[:has_status]-> s1,
        s2 <-[:has_status]- m -[:updates]-> n
      WHERE s1 <> s2
      RETURN s1.name AS from, s2.name AS to, count(*) AS count
      ORDER BY from, count DESC;
      """ format (nodes)

    val engine = new ExecutionEngine(db.service)
    val result = engine.execute(query)

    var lastFrom = ""
    var lastCount = 0L
    var nodeNames: Map[String, String] = Map()
    var lines: List[String] = Nil

    for (row <- result) {
      val from = row("from").asInstanceOf[String]
      val to = row("to").asInstanceOf[String]
      val count = row("count").asInstanceOf[Long]

      val first = if (from != lastFrom) {
        lastFrom = from
        lastCount = count
        true
      } else
        false

      val fromNodeName = nodeName(from)
      val toNodeName = nodeName(to)

      nodeNames += fromNodeName -> from
      nodeNames += toNodeName -> to
      lines = getEdgeString(fromNodeName, toNodeName, count, count.doubleValue / lastCount, first) :: lines
    }

    val graphText = "digraph {"+
      getNodesStrings(nodeNames).mkString("\n\t", "\n\t", "\n") +
      lines.mkString("\n\t", "\n\t", "\n")+
      "}"

    val graph = new DotWrapper(new File(options("dot"))).run(graphText)

    waitDialog.hide
    new ImageDialog(graph).visible = true
  }

  def getEdgeString(from: String, to: String, count: Long, factor: Double, first: Boolean) =
    if (first) {
      """%s -> %s [weight = 0, label = "%s", penwidth = 3, color = "#ff0000", fontcolor="#ff0000"];""" format (
        from, to, count)
    } else {
      val weight = ((1 - factor) * 200).intValue
      val width = (3 * factor) max 1
      val red = (255 - factor * 255).intValue
      """%s -> %s [weight = %d, label = "%s", penwidth = %f, color = "#%2x0000", fontcolor="#%6$2x0000"];""" formatLocal (
        Locale.ENGLISH,
        from, to, weight, count, width, red)
    }

  def nodeName(s: String) = s
    .replaceAll("[^A-Za-z0-9]+", "_")
    .replaceAll("^_+|_+$", "")

  def getNodesStrings(names: Map[String, String]): Iterable[String] =
    names.map {
      case (id, text) =>
        """%s [text = "%s"];""" format (id, text)
    }
}