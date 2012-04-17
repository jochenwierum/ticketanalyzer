package de.jowisoftware.mining.shell

import java.awt.Dimension
import java.io.File
import scala.swing.BorderPanel.Position
import scala.swing.event.{ WindowClosing, ButtonClicked }
import scala.swing.{ TextField, SplitPane, Orientation, Frame, Dialog, Button, BorderPanel }
import scala.swing.Dialog.Message
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.kernel.{ EmbeddedReadOnlyGraphDatabase, AbstractGraphDatabase }
import scala.swing.TextArea

class MainUI(dbFile: File) extends Frame { self =>
  private val db = new EmbeddedReadOnlyGraphDatabase(dbFile.getAbsolutePath)
  private val engine = new ExecutionEngine(db);

  private val textInput = new TextArea
  textInput.text = "START f=node(20)\nMATCH f-[x]->to\nRETURN f, to, x, type(x)"
  private val startButton = new Button(">>")
  private val textPanel = new BorderPanel() {
    layout(textInput) = Position.Center
    layout(startButton) = Position.East
  }
  private val resultTable = new ResultTablePane

  title = "Database Shell"

  contents = new SplitPane(Orientation.Horizontal, textPanel, resultTable)

  listenTo(startButton)
  reactions += {
    case WindowClosing(`self`) =>
      db.shutdown()
      dispose()
    case ButtonClicked(`startButton`) =>
      doSearch()
  }

  def run() {
    size = new Dimension(640, 480)
    visible = true
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