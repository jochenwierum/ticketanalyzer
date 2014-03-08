package de.jowisoftware.mining.gui

import java.awt.Dimension
import scala.swing.{ Dialog, Frame, Label, ProgressBar, Swing }
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position
import grizzled.slf4j.Logging
import javax.swing.WindowConstants

class ProgressDialog(p: Frame) extends ProgressMonitor with Logging {
  private object window extends Dialog(p) {
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

    def updateStatusVisibility() {
      status.visible = true
      val oldDimension = size
      size = new Dimension(oldDimension.width, oldDimension.height + status.preferredSize.height)
      centerOnScreen()
    }
  }

  @volatile private var start: Long = _
  private var _max = 1L
  private var _value = 0L

  def show() = {
    start = System.currentTimeMillis
    window.visible = true
  }

  def hide() = {
    if (start > 0) {
      info("UI was locked for "+(System.currentTimeMillis - start)+" ms")
      start = 0
    }
    window.visible = false
    window.dispose
  }

  def max = _max
  def max_=(value: Long) = {
    _max = value
    calc()
  }

  def progress = _value
  def progress_=(value: Long) = {
    _value = value
    calc()
  }

  def tick() = progress += 1

  def update(value: Long, max: Long) {
    _value = value
    _max = max
    calc()
  }

  def status: String = window.status.text
  def status_=(text: String) {
    Swing.onEDT {
      window.status.text = text
      if (!window.status.visible) {
        window.updateStatusVisibility()
      }
    }
  }

  private def calc() {
    if (_max > 0) {
      Swing.onEDT {
        window.bar.indeterminate = false
        window.bar.labelPainted = true
        window.bar.value = (10000.0 * _value / _max).asInstanceOf[Int]
      }
    }
  }
}