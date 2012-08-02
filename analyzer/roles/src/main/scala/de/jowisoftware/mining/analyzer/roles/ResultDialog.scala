package de.jowisoftware.mining.analyzer.roles

import de.jowisoftware.mining.analyzer.data.TextMatrix
import scala.swing.Dialog
import scala.swing.Frame
import de.jowisoftware.mining.analyzer.data.TextMatrixSwingTable

class ResultDialog(matrix: TextMatrix, parent: Frame) extends Dialog(parent) {
  title = "Project Roles"

  contents = new TextMatrixSwingTable(matrix)

  modal = true
  pack()
  centerOnScreen()
}