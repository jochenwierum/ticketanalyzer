package de.jowisoftware.mining.analyzer.workflow

import scala.swing.Component
import java.awt.image.BufferedImage
import java.awt.Dimension
import scala.swing.Dialog
import java.awt.Graphics2D
import scala.swing.Frame

class ImageDialog(image: BufferedImage, parent: Frame) extends Dialog(parent) {
  class DotImage extends Component {
    val imageSize = new Dimension(image.getWidth, image.getHeight)
    preferredSize = imageSize
    maximumSize = imageSize

    override def paintComponent(g: Graphics2D) {
      val winSize = size
      g.drawImage(image, 0, 0, winSize.width, winSize.height,
          0, 0, imageSize.width, imageSize.height, this.peer)
    }
  }

  title = "Ticket state workflow structure"

  val imageComponent = new DotImage()
  contents = imageComponent

  resizable = true
  modal = true
  pack
  centerOnScreen
}