package de.jowisoftware.mining.analyzer.reviews

import scala.swing.Window
import scala.swing.Dialog
import scala.swing.Frame
import de.jowisoftware.mining.gui.results.ResultTablePane
import de.jowisoftware.mining.analyzer.data.TextMatrixSwingTable
import de.jowisoftware.mining.analyzer.data.TextMatrix

class ResultDialog(matrix: TextMatrix, parent: Frame) extends Dialog(parent) {
  title = "Reviewing Distribution"

  contents = new TextMatrixSwingTable(matrix, true)

  modal = true
  pack()
  centerOnScreen()
}