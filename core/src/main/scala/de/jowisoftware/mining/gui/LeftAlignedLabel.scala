package de.jowisoftware.mining.gui

import scala.swing.Label
import scala.swing.Alignment

class LeftAlignedLabel(text: String) extends Label(text) {
  horizontalAlignment = Alignment.Left
}