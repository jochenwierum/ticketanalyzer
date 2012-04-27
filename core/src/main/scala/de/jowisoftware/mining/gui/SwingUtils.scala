package de.jowisoftware.mining.gui

import javax.swing.SwingUtilities

object SwingUtils {
  def invokeAsync(block: => Unit) {
    if (SwingUtilities.isEventDispatchThread)
      block
    else
      SwingUtilities.invokeLater(new Runnable() {
        def run = block
      })
  }
}