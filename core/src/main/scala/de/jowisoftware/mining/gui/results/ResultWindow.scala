package de.jowisoftware.mining.gui.results

import de.jowisoftware.mining.analyzer._
import scala.swing._
import scala.swing.event._
import de.jowisoftware.mining.analyzer.TextResult
import de.jowisoftware.mining.analyzer.ImageResult
import de.jowisoftware.mining.analyzer.AnalyzerResult

class ResultWindow(parent: Frame, result: AnalyzerResult) extends Dialog() {
  private val content = new BorderPanel
  content.layout(createResultPane) = BorderPanel.Position.Center

  if (!result.description.isEmpty)
    content.layout(new Label(s"<html>${result.description}</html>")) =
      BorderPanel.Position.North

  contents = content

  reactions += {
    case WindowClosing(_) => dispose()
  }

  closeOperation()
  title = result.title
  resizable = true
  size = new Dimension(640, 480)
  centerOnScreen()

  private def createResultPane = result match {
    case r: ImageResult =>
      new PicturePane(r)
    case t: TextResult =>
      new TextPane(t)
    case r: NodeResult =>
      new NodeTablePane(r)
    case m: MatrixResult =>
      new MatrixPane(m)
  }
}