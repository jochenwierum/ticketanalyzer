package de.jowisoftware.mining.gui.shell

import java.net.URI
import scala.swing._
import scala.swing.BorderPanel.Position
import scala.swing.event.ButtonClicked

import grizzled.slf4j.Logging
import org.neo4j.cypher.ExecutionEngine

import de.jowisoftware.mining.{ Main => MainApp }
import de.jowisoftware.mining.analyzer.NodeResult
import de.jowisoftware.mining.gui.{ GuiTab, ProgressDialog }
import de.jowisoftware.mining.gui.components.Link
import de.jowisoftware.mining.gui.results.NodeTablePane
import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.mining.model.nodes.{ Component => MiningComponent }
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.content.IndexedNodeCompanion

class ShellPane(db: Database, parent: Frame) extends SplitPane with GuiTab with Logging {
  private val textInput = new TextArea
  private val startButton = new Button(">>")
  private val link = new Link(new URI("http://localhost:7474"), "open neo4j console in browser")

  private val textPanel = new BorderPanel() {
    layout(new ScrollPane(textInput)) = Position.Center
    layout(startButton) = Position.East

    if (!MainApp.compactMode) {
      layout(link) = Position.South
    }
  }

  leftComponent = textPanel
  rightComponent = new Label("Please submit a query")

  listenTo(startButton)
  reactions += {
    case ButtonClicked(`startButton`) =>
      doSearch()
  }

  def align = dividerLocation = 0.2

  private def doSearch() {
    val text = textInput.text

    val dialog = new ProgressDialog(parent)
    new Thread("query-thread") {
      override def run() = {
        db.inTransaction { transaction =>
          val start = System.currentTimeMillis

          val component = try {
            info("Executing: "+text)
            val result = transaction.cypher(text)
            new NodeTablePane(new NodeResult(result, ""))
          } catch {
            case e: Exception =>
              warn("Error while executing user query", e)
              new Label("<html>Could not execute query: <br /><pre>"+
                maskHTML(e.getMessage)+"</pre></html>")
          }

          Swing.onEDT {
            dialog.hide()
            val oldLocation = dividerLocation
            rightComponent = component
            dividerLocation = oldLocation
          }
          warn("Query finished in "+(System.currentTimeMillis - start)+" ms")
        }
      }
    }.start()
    dialog.show()
  }

  private def maskHTML(s: String) = if (s == null) {
    "(null)"
  } else {
    s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("\n", "<br />")
  }

  private var lastState = Integer.MAX_VALUE
  def newViewState(state: Int) =
    if (state < lastState) db.inTransaction { transaction =>
      val text = """|MATCH (repository:CommitRepository) --> commit
      |RETURN repository, commit
      |LIMIT 20
      |
      |// Important indizes:""".stripMargin

      val companions: List[_ <: IndexedNodeCompanion[_]] =
        Commit :: CommitRepository :: MiningComponent :: File ::
          Keyword :: Milestone :: Person :: Priority :: Reproducability :: Resolution ::
          Severity :: Status :: Tag :: Ticket :: TicketComment :: TicketRepository ::
          Type :: Version :: Nil

      val nodes = companions.map { _.cypherForAll("nodes") }

      lastState = state
      textInput.text = nodes.mkString(text, "\n// ", "\n")
    }
}
