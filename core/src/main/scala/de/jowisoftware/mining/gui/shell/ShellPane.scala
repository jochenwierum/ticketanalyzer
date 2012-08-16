package de.jowisoftware.mining.gui.shell

import java.net.URI
import scala.swing.{ BorderPanel, Button, Dialog, ScrollPane, SplitPane, TextArea }
import scala.swing.BorderPanel.Position
import scala.swing.Dialog.Message
import scala.swing.Swing
import scala.swing.event.ButtonClicked
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.kernel.AbstractGraphDatabase
import de.jowisoftware.mining.{ Main => MainApp }
import de.jowisoftware.mining.gui.{ GuiTab, ProgressDialog }
import de.jowisoftware.mining.gui.components.Link
import de.jowisoftware.mining.gui.results.ResultTablePane
import grizzled.slf4j.Logging
import scala.swing.Frame
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.model.nodes.RootNode

class ShellPane(db: Database[RootNode], parent: Frame) extends SplitPane with GuiTab with Logging {
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
  private val resultTable = new ResultTablePane

  leftComponent = textPanel
  rightComponent = resultTable

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
      override def run() {
        val start = System.currentTimeMillis
        try {
          val engine = new ExecutionEngine(db.service)
          val query = engine.prepare(text)
          info("Executing: "+query.toString)
          val result = query.execute(Map())
          resultTable.processResult(result)
          warn("Query finished in "+(System.currentTimeMillis - start)+" ms")
        } catch {
          case e: Exception =>
            Swing.onEDT {
              Dialog.showMessage(resultTable, "<html>Could not execute query: <br /><pre>"+
                maskHTML(e.getMessage)+"</pre></html>", "Error", Message.Error)
            }
        } finally {
          Swing.onEDT {
            dialog.hide()
          }
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
      val rootNode = transaction.rootNode
      val text = """START root=node(%d)
      |MATCH root-[:contains]->collection
      |RETURN root, collection
      |LIMIT 20;
      |
      |// Important node ids:
      |// %s"""

      val nodes = ("Root: "+rootNode.id) ::
        ("Commits: "+rootNode.commitRepositoryCollection.id) ::
        ("Tickets: "+rootNode.ticketRepositoryCollection.id) ::
        rootNode.commitRepositoryCollection.children.map(col => "Commits in "+col.name+": "+col.id).toList :::
        rootNode.ticketRepositoryCollection.children.map(col => "Tickets in "+col.name+": "+col.id).toList :::
        ("Persons: "+rootNode.personCollection.id) ::
        ("Status: "+rootNode.statusCollection.id) ::
        ("Keywords: "+rootNode.keywordCollection.id) ::
        Nil

      lastState = state
      textInput.text = text.format(rootNode.id, nodes.mkString("\n|// ")).stripMargin
    }
}
