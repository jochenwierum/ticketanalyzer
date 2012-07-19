package de.jowisoftware.mining.gui.shell

import java.net.URI
import scala.swing.{ BorderPanel, Dialog, Button, TextArea, SplitPane, ScrollPane }
import scala.swing.BorderPanel.Position
import scala.swing.Dialog.Message
import scala.swing.event.ButtonClicked
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.kernel.AbstractGraphDatabase
import de.jowisoftware.mining.{ Main => MainApp }
import de.jowisoftware.mining.gui.GuiTab
import de.jowisoftware.mining.gui.components.Link
import de.jowisoftware.mining.gui.results.ResultTablePane

class ShellPane(db: AbstractGraphDatabase) extends SplitPane with GuiTab {
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
    try {
      val result = engine.execute(text)
      resultTable.processResult(result)
    } catch {
      case e: Exception =>
        Dialog.showMessage(resultTable, "<html>Could not execute query: <br /><pre>"+
          maskHTML(e.getMessage)+"</pre></html>", "Error", Message.Error)
    }
  }

  private def maskHTML(s: String) =
    s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("\n", "<br />")
}
