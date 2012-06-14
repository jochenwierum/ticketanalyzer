package de.jowisoftware.mining.gui.components

import java.awt.{ Desktop, Cursor }
import java.net.URI
import scala.swing.event.MouseClicked
import scala.swing.{ Button, Alignment }
import javax.swing.BorderFactory
import scala.swing.event.ButtonClicked

class Link(val url: URI, val linkText: String) extends Button {
  text = """<HTML><U><FONT COLOR="#0000FF">"""+linkText+"""</FONT></U></HTML>"""
  tooltip = url.toString

  cursor = new Cursor(Cursor.HAND_CURSOR)

  contentAreaFilled = false
  borderPainted = false
  border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
  horizontalAlignment = Alignment.Left

  reactions += {
    case ButtonClicked(_) =>
      if (Desktop.isDesktopSupported) {
        try {
          Desktop.getDesktop.browse(url)
        } catch {
          case e: Exception =>
        }
      }
  }
}