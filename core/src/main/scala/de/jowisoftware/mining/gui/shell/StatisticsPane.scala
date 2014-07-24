package de.jowisoftware.mining.gui.shell

import javax.swing.table.DefaultTableModel

import de.jowisoftware.mining.gui.GuiTab
import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.content.IndexedNodeCompanion
import grizzled.slf4j.Logging
import org.neo4j.cypher.ExecutionEngine

import scala.swing.{Frame, ScrollPane, Swing, Table}

class StatisticsPane(db: Database, parent: Frame) extends ScrollPane with GuiTab with Logging {
  private val model = new DefaultTableModel(Array[Object]("Node type", "Count"), 0) {
    override def isCellEditable(row: Int, column: Int) = false
  }

  private val table = new Table()
  table.model = model

  contents = table

  def updateStatistics() = asyncUpdate()
  def align = {}

  private def asyncUpdate() =
    new Thread("statistics-thread") with Logging {
      setDaemon(true)

      override def run(): Unit = {
        info("Updating stats")
        try {
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
        } catch {
          case e: Exception =>
            warn("", e)
            warn(e.getClass.getName+" while building statistics - assuming the database is gone.")
        }
      }
    }.start()

  private def collectTickets(engine: ExecutionEngine): Seq[(String, Long)] = {
    val result = engine.execute(
    s"MATCH ${TicketRepository.cypherForAll("tickets")} --> ticket "+
        "RETURN tickets.name AS name, count(ticket) AS count")

    val distinctResult = engine.execute(
        s"MATCH ${TicketRepository.cypherForAll("tickets")} --> ticket "+
        "RETURN tickets.name AS name, count(distinct ticket.id) AS count")

    result.map { row =>
      ("Tickets in "+row("name").asInstanceOf[String], row("count").asInstanceOf[Long])
    }.toSeq.sortBy(_._1) ++ distinctResult.map { row =>
      ("Distinct tickets in "+row("name").asInstanceOf[String], row("count").asInstanceOf[Long])
    }.toSeq.sortBy(_._1)
  }

  private def collectCommits(engine: ExecutionEngine): Seq[(String, Long)] = {
    val commitsResult = engine.execute(
    s"MATCH ${CommitRepository.cypherForAll("commits")}  -[:contains]-> commit "+
        "RETURN commits.name AS name, count(commit) AS count")

    val filesResult = engine.execute(
    s"MATCH ${CommitRepository.cypherForAll("commits")} -[:contains_files]-> () --> file "+
        "RETURN commits.name AS name, count(file) AS count")

    commitsResult.map { row =>
      ("Commits in "+row("name").asInstanceOf[String], row("count").asInstanceOf[Long])
    }.toSeq.sortBy(_._1) ++ filesResult.map { row =>
      ("Files in "+row("name").asInstanceOf[String], row("count").asInstanceOf[Long])
    }.toSeq.sortBy(_._1)
  }

  private def collectGlobalStats(engine: ExecutionEngine): List[(String, Long)] = {
    val repositories: List[(IndexedNodeCompanion[_], String)] = (Keyword, "Keywords") :: (Severity, "Severities") :: (Status, "Status") ::
        (Person, "Persons") :: (Priority, "Priorities") :: (Milestone, "Milestones") ::
        (Type, "Types") :: (Commit, "Commits") :: (Tag, "Tags") :: (Reproducability, "Reproducabilities") ::
        (Ticket, "Tickets") :: (Resolution, "Resolutions") :: (Component, "Components") :: (Version, "Versions") :: Nil

    repositories.map { case (rep, name) =>
      name -> engine.execute(s"MATCH ${rep.cypherForAll("node")} RETURN count(node) AS c").map(row => row("c").asInstanceOf[Long]).sum
    }.sortBy(_._1)
  }
}
