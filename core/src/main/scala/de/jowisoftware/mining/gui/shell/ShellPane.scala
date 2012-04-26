package de.jowisoftware.mining.gui.shell

import scala.swing.BorderPanel.Position
import scala.swing.Dialog.Message
import scala.swing.event.ButtonClicked
import scala.swing.{ TextArea, SplitPane, Orientation, Dialog, Button, BorderPanel }

import org.neo4j.cypher.ExecutionEngine
import org.neo4j.kernel.AbstractGraphDatabase

class ShellPane(db: AbstractGraphDatabase) extends SplitPane {
  private val engine = new ExecutionEngine(db);

  private val textInput = new TextArea
  textInput.text = "START f=node(20)\nMATCH f-[x]->to\nRETURN f, to, x, type(x)"
  private val startButton = new Button(">>")
  private val textPanel = new BorderPanel() {
    layout(textInput) = Position.Center
    layout(startButton) = Position.East
  }
  private val resultTable = new ResultTablePane

  leftComponent = textPanel
  rightComponent = resultTable

  listenTo(startButton)
  reactions += {
    case ButtonClicked(`startButton`) =>
      doSearch()
  }

  private def doSearch() {
    val text = textInput.text
    val result = try {
      engine.execute(text)
    } catch {
      case e: Exception =>
        Dialog.showMessage(resultTable, "Could not execute query: "+e.getMessage, "Error", Message.Error)
        return
    }
    resultTable.processResult(result)
  }
}