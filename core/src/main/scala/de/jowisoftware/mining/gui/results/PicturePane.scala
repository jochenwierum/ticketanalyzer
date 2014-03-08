package de.jowisoftware.mining.gui.results

import java.awt.image.BufferedImage
import scala.swing._
import scala.swing.event._
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import de.jowisoftware.mining.analyzer.ImageResult

class PicturePane(image: ImageResult) extends ScrollPane {
  listenTo(mouse.clicks)
  listenTo(mouse.moves)
  listenTo(mouse.wheel)

  private val imagePanel = new Panel {
    override def paint(g: Graphics2D) = {
      g.drawImage(image.image, 0, 0, minimumSize.width, minimumSize.height, this.peer)
    }
  }
  newSize(image.image.getWidth, image.image.getHeight)

  private var start: Point = null
  private var startX = 0
  private var startY = 0
  private var zoom = 100

  reactions += {
    case e: MousePressed =>
      start = e.point
      startX = this.horizontalScrollBar.value
      startY = this.verticalScrollBar.value

    case e: MouseDragged =>
      val diffx = start.x - e.point.x
      val diffy = start.y - e.point.y

      this.horizontalScrollBar.value = startX + diffx
      this.verticalScrollBar.value = startY + diffy

    case e: MouseWheelMoved =>
      zoom += e.rotation * 4
      zoom = (zoom max 25) min 250

      newSize((image.image.getWidth * 100f / zoom).toInt, (image.image.getHeight * 100f / zoom).toInt)
  }

  private def newSize(w: Int, h: Int) {
    val size = new Dimension(w, h)
    imagePanel.minimumSize = size
    imagePanel.maximumSize = size
    imagePanel.preferredSize = size
    imagePanel.revalidate()
  }

  contents = imagePanel
}