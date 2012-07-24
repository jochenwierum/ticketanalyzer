package de.jowisoftware.mining.analyzer.structure

import scala.swing.Component
import java.awt.image.BufferedImage
import java.awt.Dimension
import scala.swing.Dialog
import java.awt.Graphics2D

class ImageDialog(image: BufferedImage) extends Dialog {
  class DotImage extends Component {
    val imageSize = new Dimension(image.getWidth, image.getHeight)
    preferredSize = imageSize
    maximumSize = imageSize

    override def paintComponent(g: Graphics2D) {
      g.drawImage(image, 0, 0, this.peer)
    }
  }

  title = "Ticket status structure"

  val imageComponent = new DotImage()
  contents = imageComponent

  resizable = false
  modal = true
  pack
  centerOnScreen
}