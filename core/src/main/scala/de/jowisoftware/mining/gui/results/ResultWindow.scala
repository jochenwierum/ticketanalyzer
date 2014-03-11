package de.jowisoftware.mining.gui.results

import de.jowisoftware.mining.analyzer._
import scala.swing._
import scala.swing.event._
import de.jowisoftware.mining.analyzer.TextResult
import de.jowisoftware.mining.analyzer.ImageResult
import de.jowisoftware.mining.analyzer.AnalyzerResult
import de.jowisoftware.mining.gui.components.ToolBar
import javax.swing.JFileChooser
import java.io.File
import javax.swing.filechooser.FileFilter
import java.io.FileOutputStream

object ResultWindow {
  var lastDir: File = null
}

class ResultWindow(parent: Frame, result: AnalyzerResult) extends Dialog() {
  private val saveButton = createSaveButton

  val resultPane: Component with ResultPane = result match {
    case r: ImageResult =>
      new ImagePane(r)
    case t: TextResult =>
      new TextPane(t)
    case r: NodeResult =>
      new NodeTablePane(r)
    case m: MatrixResult =>
      new MatrixPane(m)
  }

  contents = {
    val panel = new BorderPanel

    panel.layout(createToolBar(saveButton)) = BorderPanel.Position.North
    panel.layout(createMainPane) = BorderPanel.Position.Center

    panel
  }

  listenTo(saveButton)
  reactions += {
    case WindowClosing(_) => dispose()
    case ButtonClicked(`saveButton`) =>
      showSave()
  }

  closeOperation()
  title = result.title
  resizable = true
  size = new Dimension(640, 480)
  centerOnScreen()

  def createToolBar(buttons: Button*): ToolBar = {
    val bar = new ToolBar

    bar.peer.setFloatable(false)
    bar.contents ++= buttons

    bar
  }

  def createSaveButton = {
    val button = new Button
    button.text = "Save"
    button
  }

  def createMainPane = {
    val content = new BorderPanel

    if (!result.description.isEmpty)
      content.layout(new Label(s"<html>${result.description}</html>")) =
        BorderPanel.Position.North

    content.layout(resultPane) = BorderPanel.Position.Center

    content
  }

  def showSave(): Unit = {
    val fileChooser = new FileChooser
    fileChooser.peer.setAcceptAllFileFilterUsed(true)

    fileChooser.fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    val filter = new FileFilter() {
      def accept(f: File): Boolean = f.isDirectory || f.getName().toLowerCase().endsWith(resultPane.saveDescription.pattern)
      def getDescription: String = resultPane.saveDescription.text
    }

    fileChooser.peer.setCurrentDirectory(ResultWindow.lastDir)
    fileChooser.peer.addChoosableFileFilter(filter)
    fileChooser.fileFilter = filter

    fileChooser.showSaveDialog(resultPane)

    val selectedFileOption = Option(fileChooser.selectedFile)
    for (selectedFile <- selectedFileOption) {
      val suffix = "."+resultPane.saveDescription.pattern
      val realFileName = if (fileChooser.fileFilter == filter &&
        !selectedFile.getName().toLowerCase().endsWith(suffix.toLowerCase())) {
        new File(selectedFile.getParentFile(), selectedFile.getName() + suffix)
      } else {
        selectedFile
      }

      ResultWindow.lastDir = selectedFile.getParentFile()
      val stream = new FileOutputStream(realFileName)
      try {
        resultPane.saveToStream(stream)
      } finally {
        stream.close()
      }
    }
  }
}