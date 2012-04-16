package de.jowisoftware.mining.awt

import java.awt.{Toolkit, Point, Dimension}

import scala.swing.event.{WindowClosing, ButtonClicked}
import scala.swing.{Swing, Orientation, Label, Dialog, Button, BoxPanel, BorderPanel}

class Assistant(frameTitle: String, pages: AssistantPage*) extends Dialog {
  title = frameTitle

  private val stepTitle = new Label("")
  private val backButton = new Button("back")
  private val nextButton = new Button("next")
  private val okButton = new Button("ok")
  private val abortButton = new Button("abort")
  private var step = 0
  private var closed = false
  private var finished = false

  nextButton.reactions += {
    case ButtonClicked(_) =>
      if (pages(step).save) {
        step += 1
        updatePanel
      }
  }

  okButton.reactions += {
    case ButtonClicked(_) => finish()
  }

  abortButton.reactions += {
    case ButtonClicked(_) => abort()
  }

  backButton.reactions += {
    case ButtonClicked(_) =>
      step -= 1
      updatePanel
  }

  reactions += {
    case WindowClosing(_) if !closed => abort()
  }

  val contentPanel = new BoxPanel(Orientation.Vertical) {
    contents += pages(0).getPanel
  }

  contents = new BorderPanel() {
    layout(stepTitle) = BorderPanel.Position.North
    layout(contentPanel) = BorderPanel.Position.Center
    layout(new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += backButton
      contents += abortButton
      contents += nextButton
      contents += okButton
      contents += Swing.HGlue

      xLayoutAlignment = .5
    }) = BorderPanel.Position.South
  }

  updatePanel()

  size = new Dimension(320, 240)
  val tk = Toolkit.getDefaultToolkit().getScreenSize()
  location = new Point((tk.getWidth() - size.width).toInt / 2,
    (tk.getHeight() - size.height).toInt / 2)

  private def abort() {
    finished = false;
    closed = true
    dispose()
  }

  private def finish() {
    finished = true
    closed = true
    dispose()
  }

  private def updatePanel() {
    stepTitle.text = pages(step).title

    contentPanel.contents.clear
    contentPanel.contents += pages(step).getPanel

    backButton.enabled = step > 0
    nextButton.enabled = step < pages.size - 1
    okButton.enabled = step == pages.size - 1
  }
}