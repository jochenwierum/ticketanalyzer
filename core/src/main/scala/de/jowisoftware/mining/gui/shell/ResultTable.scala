package de.jowisoftware.mining.gui.shell

import java.awt.Color
import scala.swing.{ Table, Label, Component, Alignment }
import javax.swing.BorderFactory
import javax.swing.UIManager
import sun.swing.DefaultLookup
import javax.swing.table.TableModel
import java.awt.Graphics2D
import java.awt.FontMetrics

class ResultTable extends Table { that =>
  private var sizeSet = false

  override def model_=(newModel: TableModel) = {
    sizeSet = false
    super.model = newModel
  }

  override def paintComponent(g: Graphics2D) {
    if (!sizeSet) {
      calcLineHeight(g.getFontMetrics(this.font))
      sizeSet = true
      return
    }
    super.paintComponent(g)
  }

  private def calcLineHeight(fm: FontMetrics) {
    val lines = for {
      row <- 0 until model.getRowCount;
      col <- 0 until model.getColumnCount;
      v = model.getValueAt(row, col).asInstanceOf[CellData].shortText
    } yield v.count(_ == '\n') + 1

    val maxLines = lines.reduce((a, b) => a max b)
    rowHeight = fm.getHeight * maxLines
  }

  override def rendererComponent(isSelected: Boolean,
    hasFocus: Boolean, row: Int, column: Int): Component = {
    val value = model.getValueAt(row, column).asInstanceOf[CellData]
    new Label("<html>"+value.shortText+"</html>") {
      tooltip = "<html>"+value.longText+"</html>"

      opaque = true

      foreground = if (isSelected)
        that.selectionForeground
      else
        that.foreground

      background = if (isSelected) {
        that.selectionBackground
      } else if (row % 2 == 1) {
        UIManager.getColor("Table.alternateRowColor")
      } else {
        that.background
      }

      font = that.font
      xAlignment = Alignment.Left
      yAlignment = Alignment.Top
    }
  }
}