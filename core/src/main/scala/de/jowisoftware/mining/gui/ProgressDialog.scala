package de.jowisoftware.mining.gui

import java.awt.Dimension
import scala.swing.BorderPanel.Position
import scala.swing.{ ProgressBar, Label, Frame, Dialog, BorderPanel }
import javax.swing.JComponent
import scala.swing.event.WindowClosing
import javax.swing.WindowConstants

class ProgressDialog(p: Frame) extends Dialog(p) {
  private var _max = 1L
  private var _value = 0L

  title = "Progress"
  modal = true
  size = new Dimension(500, 140)
  centerOnScreen()

  peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  val bar = new ProgressBar()
  bar.indeterminate = true
  bar.labelPainted = true
  bar.max = 10000
  bar.min = 0
  bar.value = 0

  contents = new BorderPanel {
    layout(new Label("Please wait...")) = Position.North
    layout(bar) = Position.Center
  }

  def show() = visible = true

  def hide() = {
    visible = false
    dispose
  }

  def max = bar.max
  def max_=(value: Long) = {
    _max = value
    calc()
  }

  def progress = bar.value
  def progress_=(value: Long) = {
    _value = value
    calc()
  }

  private def calc() {
    if (_max > 0) {
      bar.indeterminate = false
      bar.value = (10000.0 * (1.0 * _value / _max)).asInstanceOf[Int]
    }
  }
}