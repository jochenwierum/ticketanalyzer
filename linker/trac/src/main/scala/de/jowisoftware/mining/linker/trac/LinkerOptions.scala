package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.UserOptions
import scala.swing.GridPanel
import scala.swing.Panel

class LinkerOptions extends UserOptions {
  protected var result: Map[String, String] = Map()

  def getPanel(): Panel = new GridPanel(3, 2) {

  }
}