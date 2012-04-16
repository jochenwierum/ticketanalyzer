package de.jowisoftware.mining.awt
import scala.swing.Frame
import javax.swing.UIManager
import de.jowisoftware.mining.plugins.PluginManager
import scala.swing.Label
import java.io.File
import scala.swing.event.WindowClosing
import java.awt.Toolkit
import java.awt.Point

class MainWindow(pluginManager: PluginManager) extends Frame {
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
  title = "test"

  /*
  val pluginPanel = new PluginPanel(pluginManager, this)
  contents = pluginPanel
*/

  reactions += {
    case WindowClosing(_) => dispose()
  }

  pack
  val tk = Toolkit.getDefaultToolkit().getScreenSize()
  location = new Point((tk.getWidth() - size.width).toInt / 2,
    (tk.getHeight() - size.height).toInt / 2)
}