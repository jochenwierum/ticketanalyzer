package de.jowisoftware.mining.external.dot

import scala.swing.Component
import java.awt.image.BufferedImage
import java.awt.Dimension
import scala.swing.Dialog
import java.awt.Graphics2D
import scala.swing.Frame
import javax.swing.JScrollPane
import scala.swing.ScrollPane
import scala.swing.event.ComponentResized
import scala.swing.event.ComponentResized
import scala.swing.event.UIElementResized

class ImageDialog(image: BufferedImage, parent: Frame, frameTitle: String) extends Dialog(parent) {
  class ImageViewer extends Component {
    val imageSize = new Dimension(image.getWidth, image.getHeight)
    preferredSize = imageSize
    maximumSize = imageSize

    override def paintComponent(g: Graphics2D) {
      val winSize = size
      g.drawImage(image, 0, 0, winSize.width, winSize.height,
        0, 0, imageSize.width, imageSize.height, ImageViewer.this.peer)
    }
  }

  title = frameTitle

  private val imageComponent = new ImageViewer()
  private val content = new ScrollPane(imageComponent)

  content.minimumSize = new Dimension(300, 200)
  content.preferredSize = new Dimension(imageComponent.preferredSize.width min 800,
    imageComponent.preferredSize.height min 600)
  content.maximumSize = new Dimension(imageComponent.maximumSize.width + 1,
    imageComponent.maximumSize.height + 1)

  contents = content

  preferredSize = content.preferredSize
  maximumSize = content.maximumSize
  minimumSize = content.minimumSize

  resizable = true
  modal = true

  pack
  centerOnScreen
}