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

class ShellPane(db: AbstractGraphDatabase, parent: Frame) extends SplitPane with GuiTab with Logging {
  private val engine = new ExecutionEngine(db)

  private val textInput = new TextArea
  textInput.text = "START f=node(20)\nMATCH f-[x]->to\nRETURN f, to, x, type(x)"
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
          val result = engine.execute(text)
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

  private def maskHTML(s: String) =
    s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("\n", "<br />")
}
