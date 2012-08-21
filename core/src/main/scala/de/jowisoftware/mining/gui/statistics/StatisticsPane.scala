package de.jowisoftware.mining.gui.shell

import scala.swing.{ Frame, ScrollPane, Swing, Table }

import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb.Node

import de.jowisoftware.mining.gui.GuiTab
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database
import grizzled.slf4j.Logging
import javax.swing.table.DefaultTableModel

class StatisticsPane(db: Database[RootNode], parent: Frame) extends ScrollPane with GuiTab with Logging {
  private val model = new DefaultTableModel(Array[Object]("Node type", "Count"), 0) {
    override def isCellEditable(row: Int, column: Int) = false
  }

  private val table = new Table()
  table.model = model

  contents = table

  asyncUpdate

  def updateStatistics() = asyncUpdate
  def align = {}

  private def asyncUpdate() =
    new Thread("statistics-thread") {
      override def run() {
        val start = System.currentTimeMillis
        val engine = new ExecutionEngine(db.service)

        val tickets = collectTickets(engine)
        val commits = collectCommits(engine)
        val globalValues = collectGlobalStats(engine)

        val rows = tickets ++ commits ++ globalValues

        Swing.onEDT {
          model.setRowCount(0)
          rows.foreach(row => model.addRow(Array[Object](row._1, row._2.toString)))
        }
        warn("Updating statistics finished in "+(System.currentTimeMillis - start)+" ms")
      }
    }.start()

  private def collectTickets(engine: ExecutionEngine): Seq[(String, Long)] = {
    val result = engine.execute(
      "START root = node(0) "+
        "MATCH root --> ticketrepositories --> tickets --> ticket "+
        "WHERE ticketrepositories._class =~ /.*TicketRepositoryRepository/ "+
        "RETURN tickets.name AS name, count(ticket) AS count")

    val distinctResult = engine.execute(
      "START root = node(0) "+
        "MATCH root --> ticketrepositories --> tickets --> ticket "+
        "WHERE ticketrepositories._class =~ /.*TicketRepositoryRepository/ "+
        "RETURN tickets.name AS name, count(distinct ticket.id) AS count")

    result.map { row =>
      ("Tickets in "+row("name").asInstanceOf[String], row("count").asInstanceOf[Long])
    }.toSeq.sortBy(_._1) ++ distinctResult.map { row =>
      ("Distinct tickets in "+row("name").asInstanceOf[String], row("count").asInstanceOf[Long])
    }.toSeq.sortBy(_._1)
  }

  private def collectCommits(engine: ExecutionEngine): Seq[(String, Long)] = {
    val commitsResult = engine.execute(
      "START root = node(0) "+
        "MATCH root --> commitrepositories --> commits -[:contains]-> commit "+
        "WHERE commitrepositories._class =~ /.*CommitRepositoryRepository/ "+
        "RETURN commits.name AS name, count(commit) AS count")

    val filesResult = engine.execute(
      "START root = node(0) "+
        "MATCH root --> commitrepositories --> commits -[:contains_files]-> () --> file "+
        "WHERE commitrepositories._class =~ /.*CommitRepositoryRepository/ "+
        "RETURN commits.name AS name, count(file) AS count")

    commitsResult.map { row =>
      ("Commits in "+row("name").asInstanceOf[String], row("count").asInstanceOf[Long])
    }.toSeq.sortBy(_._1) ++ filesResult.map { row =>
      ("Files in "+row("name").asInstanceOf[String], row("count").asInstanceOf[Long])
    }.toSeq.sortBy(_._1)
  }

  private def collectGlobalStats(engine: ExecutionEngine): Seq[(String, Long)] = {
    val result = engine.execute(
      "START root = node(0) "+
        "MATCH root --> node --> children "+
        "RETURN node AS node, count(children) AS count")

    result.flatMap { row =>
      val node = row("node").asInstanceOf[Node]
      val count = row("count").asInstanceOf[Long]

      val classProperty = node.getProperty("_class").asInstanceOf[String]
      val className = classProperty.substring(classProperty.lastIndexOf('.') + 1)

      val title = className match {
        case "KeywordRepository" => Some("Keywords")
        case "SeverityRepository" => Some("Severities")
        case "StatusRepository" => Some("Status")
        case "PersonRepository" => Some("Persons")
        case "PriorityRepository" => Some("Priorities")
        case "MilestoneRepository" => Some("Milestones")
        case "TypeRepository" => Some("Types")
        case "CommitRepositoryRepository" => Some("Commit repositories")
        case "TagRepository" => Some("Tags")
        case "ReproducabilityRepository" => Some("Reproducabilities")
        case "TicketRepositoryRepository" => Some("Ticket repositories")
        case "ResolutionRepository" => Some("Resolutions")
        case "ComponentRepository" => Some("Components")
        case "VersionRepository" => Some("Versions")
        case _ => None
      }
      title.map { t => (t, count) }
    }.toSeq.sortBy(_._1)
  }
}
