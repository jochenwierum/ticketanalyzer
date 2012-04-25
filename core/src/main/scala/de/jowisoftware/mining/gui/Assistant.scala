package de.jowisoftware.mining.gui

import java.awt.Dimension
import scala.swing.event.ButtonClicked
import scala.swing.event.WindowClosing
import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.Frame

class Assistant(frameTitle: String, owner: Frame, pages: AssistantPage*) extends Dialog(owner) {
  private val stepTitle = new Label("")
  private val backButton = new Button("back")
  private val nextButton = new Button("next")
  private val okButton = new Button("ok")
  private val abortButton = new Button("abort")
  private var step = 0
  private var closed = false
  private var finished = false

  title = frameTitle
  modal = true

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

  size = new Dimension(320, 240)
  updatePanel()
  centerOnScreen

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

  def run() = {
    visible = true
    finished
  }
}