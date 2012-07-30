package de.jowisoftware.mining.gui

import java.awt.Dimension

import scala.swing.BorderPanel.Position
import scala.swing.{ Swing, ProgressBar, Label, Frame, Dialog, BorderPanel }

import javax.swing.WindowConstants

class ProgressDialog(p: Frame) extends Dialog(p) {
  private var _max = 1L
  private var _value = 0L

  title = "Progress"
  peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  val bar = new ProgressBar()
  bar.indeterminate = true
  bar.max = 10000
  bar.min = 0
  bar.value = 0

  val status = new Label()
  status.visible = false

  contents = new BorderPanel {
    layout += new Label("Please wait...") -> Position.North
    layout += bar -> Position.Center
    layout += status -> Position.South
  }

  modal = true
  size = new Dimension(200, 70)
  centerOnScreen()

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

  def update(value: Long, max: Long) {
    _value = value
    _max = max
    calc()
  }

  def status(text: String) {
    Swing.onEDT {
      status.text = text
      if (!status.visible) {
        updateStatusVisibility()
      }
    }
  }

  private def calc() {
    if (_max > 0) {
      Swing.onEDT {
        bar.indeterminate = false
        bar.labelPainted = true
        bar.value = (10000.0 * (1.0 * _value / _max)).asInstanceOf[Int]
      }
    }
  }

  private def updateStatusVisibility() {
    status.visible = true
    val oldDimension = this.size
    this.size = new Dimension(oldDimension.width, oldDimension.height + status.preferredSize.height)
    centerOnScreen()
  }
}